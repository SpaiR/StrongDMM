package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.common.ViewController
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.render.VisualComposer
import io.github.spair.strongdmm.primaryFrame
import org.kodein.di.direct
import org.kodein.di.erased.instance
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import kotlin.concurrent.thread

class MapCanvasController : ViewController<MapCanvasView>(DI.direct.instance()) {

    private val visualComposer by DI.instance<VisualComposer>()
    private val inputProcessor = InputProcessor(this)

    private var selectedMap: Dmm? = null
    private var glInitialized = false
    private var frameInitialized = false

    // Visual offset to translate viewport
    private var xViewOff = 0f
    private var yViewOff = 0f

    // Map offset with coords for bottom-left point of the screen
    private var xMapOff = 0
    private var yMapOff = 0

    // Zooming stuff
    private var viewZoom = 1f
    private val zoomFactor = 1.5f
    private var currZoom = 5
    private val maxZoomOut = 0
    private val maxZoomIn = 10

    override fun init() {
    }

    fun selectMap(dmm: Dmm) {
        selectedMap = dmm

        if (!glInitialized) {
            initGLDisplay()
        }

        Frame.update(true)
    }

    fun updateViewAndMapOffset(x: Int, y: Int) {
        val xViewOffNew = xViewOff + x * viewZoom
        val yViewOffNew = yViewOff + y * viewZoom

        if (xViewOffNew != xViewOff || yViewOffNew != yViewOff) {
            xViewOff = xViewOffNew
            yViewOff = yViewOffNew

            updateMapOffset()
            Frame.update()
        }
    }

    fun updateZoom(isZoomIn: Boolean) {
        if ((!isZoomIn && currZoom - 1 < maxZoomOut) || (isZoomIn && currZoom + 1 > maxZoomIn)) {
            return
        }

        currZoom += if (isZoomIn) 1 else -1


        if (isZoomIn) {
            viewZoom /= zoomFactor
            xViewOff -= Mouse.getX() * viewZoom / 2
            yViewOff -= Mouse.getY() * viewZoom / 2
        } else {
            xViewOff += Mouse.getX() * viewZoom / 2
            yViewOff += Mouse.getY() * viewZoom / 2
            viewZoom *= zoomFactor
        }

        updateMapOffset()
        Frame.update()
    }

    private fun updateMapOffset() {
        xMapOff = (-xViewOff / selectedMap!!.iconSize + 0.5f).toInt()
        yMapOff = (-yViewOff / selectedMap!!.iconSize + 0.5f).toInt()
    }

    private fun initGLDisplay() {
        if (!frameInitialized) {
            initFrameUpdateListeners()
            frameInitialized = true
        }

        thread(start = true) {
            glInitialized = true
            Display.setParent(view.canvas)
            Display.create()
            Display.setVSyncEnabled(true)
            startRenderLoop()
            Display.destroy()
            glInitialized = false
        }
    }

    private fun initFrameUpdateListeners() {
        // Update frames on simple window resize
        view.canvas.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                Frame.update()
            }
        })

        // Update frames when window minimized/maximized
        primaryFrame().windowFrame.addWindowStateListener {
            Frame.update()
        }
    }

    private fun startRenderLoop() {
        glClearColor(1f, 1f, 1f, 1f)

        glEnable(GL_TEXTURE_2D)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        while (!Display.isCloseRequested() && selectedMap != null) {
            if (Frame.hasUpdates()) {
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                val width = Display.getWidth()
                val height = Display.getHeight()

                glViewport(0, 0, width, height)

                glMatrixMode(GL_PROJECTION)
                glLoadIdentity()
                glOrtho(0.0, getViewWidth(), 0.0, getViewHeight(), 1.0, -1.0)
                glMatrixMode(GL_MODELVIEW)
                glLoadIdentity()
                glTranslatef(xViewOff, yViewOff, 0f)

                // actual map rendering
                renderSelectedMap()

                Display.update(false)
            }

            Display.processMessages()
            inputProcessor.fire()
            Display.sync(60)
        }
    }

    private fun renderSelectedMap() {
        val map = selectedMap!!

        val horTilesNum = (getViewWidth() / map.iconSize + 0.5f).toInt()
        val verTilesNum = (getViewHeight() / map.iconSize + 0.5f).toInt()

        val frameRenderInstances = visualComposer.composeFrame(map, xMapOff, yMapOff, horTilesNum, verTilesNum, Frame.isForced())
        var bindedTexture = -1

        frameRenderInstances.values.forEach { plane ->
            plane.values.forEach { layer ->
                layer.forEach { ri ->
                    glColor4f(ri.color.red, ri.color.green, ri.color.blue, ri.color.alpha)

                    if (ri.textureId != bindedTexture) {
                        glBindTexture(GL_TEXTURE_2D, ri.textureId)
                        bindedTexture = ri.textureId
                    }

                    glPushMatrix()
                    glTranslatef(ri.locX, ri.locY, 0f)

                    glBegin(GL_QUADS)
                    with(ri) {
                        glTexCoord2f(u2, v1)
                        glVertex2f(width, height)

                        glTexCoord2f(u1, v1)
                        glVertex2f(0f, height)

                        glTexCoord2f(u1, v2)
                        glVertex2f(0f, 0f)

                        glTexCoord2f(u2, v2)
                        glVertex2f(width, 0f)
                    }
                    glEnd()

                    glPopMatrix()
                }
            }
        }

        if (visualComposer.hasIncompleteJob) {
            Frame.update()
        }
    }

    private fun getViewWidth() = Display.getWidth() * viewZoom.toDouble()
    private fun getViewHeight() = Display.getHeight() * viewZoom.toDouble()
}
