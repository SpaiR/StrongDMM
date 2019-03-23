package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.common.ViewController
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.render.VisualComposer
import io.github.spair.strongdmm.primaryFrame
import org.kodein.di.direct
import org.kodein.di.erased.instance
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import kotlin.concurrent.thread

class MapCanvasController : ViewController<MapCanvasView>(DI.direct.instance()) {

    private val visualComposer by DI.instance<VisualComposer>()
    private val inputProcessor = InputProcessor(this)

    private var glInitialized = false
    private var frameInitialized = false

    var selectedMap: Dmm? = null

    // Visual offset to translate viewport
    var xViewOff = 0f
    var yViewOff = 0f

    // Map offset with coords for bottom-left point of the screen
    var xMapOff = 0
    var yMapOff = 0

    // Zooming stuff
    var viewZoom = 1f
    val zoomFactor = 1.5f
    var currZoom = 5
    val maxZoomOut = 0
    val maxZoomIn = 10

    override fun init() {
    }

    fun selectMap(dmm: Dmm) {
        selectedMap = dmm

        if (!glInitialized) {
            initGLDisplay()
        }

        Frame.update(true)
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
        glClearColor(0.25f, 0.25f, 0.5f , 1f)

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

        val renderInstances = visualComposer.composeFrame(map, xMapOff, yMapOff, horTilesNum, verTilesNum, Frame.isForced())
        var bindedTexture = -1

        renderInstances.values.forEach { plane ->
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
