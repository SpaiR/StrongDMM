package io.github.spair.strongdmm.gui.map

import io.github.spair.strongdmm.gui.map.input.KeyboardProcessor
import io.github.spair.strongdmm.gui.map.input.MouseProcessor
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.OUT_OF_BOUNDS
import io.github.spair.strongdmm.logic.render.VisualComposer
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import java.awt.Canvas
import kotlin.concurrent.thread

class MapPipeline(private val canvas: Canvas) {

    private var glInitialized = false

    var selectedMap: Dmm? = null
    var iconSize = 32

    // Visual offset to translate viewport
    var xViewOff = 0f
    var yViewOff = 0f

    // Map offset with coords for bottom-left point of the screen
    var xMapOff = 0
    var yMapOff = 0

    // Coords of tile where the mouse is
    var xMouseMap = 0
    var yMouseMap = 0

    // Zooming stuff
    var viewZoom = 1f
    val zoomFactor = 1.5f
    var currZoom = 5
    val maxZoomOut = 0
    val maxZoomIn = 10

    // Coords of pixel on the map where the mouse is
    var xMouse = 0f
    var yMouse = 0f

    // When true, while next rendering loop, top item under the mouse will be selected
    var isSelectItem = false

    init {
        MouseProcessor.mapPipeline = this
    }

    fun switchMap(map: Dmm) {
        selectedMap = map
        iconSize = map.iconSize

        if (!glInitialized) {
            initGLDisplay()
        }

        Frame.update(true)
    }

    private fun initGLDisplay() {
        thread(start = true) {
            glInitialized = true
            Display.setParent(canvas)
            Display.create()
            DmiProvider.initTextures()
            startRenderLoop()  // this is where the magic happens
            DmiProvider.clearTextures()
            VisualComposer.clearCache()
            Display.destroy()
            glInitialized = false
        }
    }

    private fun startRenderLoop() {
        glClearColor(0.25f, 0.25f, 0.5f , 1f)

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

                // actual rendering
                renderMap()
                renderMousePosition()
                SelectOperation.render(iconSize)

                Display.update(false)
            }

            Display.processMessages()
            KeyboardProcessor.fire()
            MouseProcessor.fire()
            Display.sync(60)
        }
    }

    private fun renderMap() {
        val horTilesNum = (getViewWidth() / iconSize + 0.5f).toInt()
        val verTilesNum = (getViewHeight() / iconSize + 0.5f).toInt()

        val renderInstances = VisualComposer.composeFrame(selectedMap!!, xMapOff, yMapOff, horTilesNum, verTilesNum, Frame.isForced())
        var bindedTexture = -1

        glEnable(GL_TEXTURE_2D)

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
                        glVertex2i(width, height)

                        glTexCoord2f(u1, v1)
                        glVertex2i(0, height)

                        glTexCoord2f(u1, v2)
                        glVertex2i(0, 0)

                        glTexCoord2f(u2, v2)
                        glVertex2i(width, 0)
                    }
                    glEnd()

                    glPopMatrix()
                }
            }
        }

        glDisable(GL_TEXTURE_2D)

        // Postponed images will be loaded in next frame
        if (VisualComposer.hasIncompleteJob) {
            Frame.update()
        }

        if (isSelectItem) {
            isSelectItem = false
            findAndSelectItemUnderMouse(renderInstances)
        }
    }

    private fun renderMousePosition() {
        if (xMouseMap == OUT_OF_BOUNDS || yMouseMap == OUT_OF_BOUNDS) {
            return
        }

        val xPos = (xMouseMap - 1) * iconSize
        val yPos = (yMouseMap - 1) * iconSize

        glColor4f(1f, 1f, 1f, 0.25f)

        glBegin(GL_QUADS)
        run {
            glVertex2i(xPos, yPos)
            glVertex2i(xPos + iconSize, yPos)
            glVertex2i(xPos + iconSize, yPos + iconSize)
            glVertex2i(xPos, yPos + iconSize)
        }
        glEnd()
    }

    private fun getViewWidth() = Display.getWidth() * viewZoom.toDouble()
    private fun getViewHeight() = Display.getHeight() * viewZoom.toDouble()
}
