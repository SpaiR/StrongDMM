package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.primaryFrame
import io.github.spair.strongdmm.logic.map.Dmm
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import java.awt.Canvas
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import kotlin.concurrent.thread

class MapDrawerGL(canvas: Canvas) {

    private var currentMap: Dmm? = null

    init {
        thread(start = true) {
            Display.setParent(canvas)
            Display.create()

            initFrameUpdateListeners(canvas)
            startRenderLoop()

            Display.destroy()
        }
    }

    fun switchMap(dmm: Dmm) {
        currentMap = dmm
    }

    private fun initFrameUpdateListeners(canvas: Canvas) {
        // Update frames on simple window resize
        canvas.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                FrameUpdater.updateFrames()
            }
        })

        // Update frames when window minimized / maximized
        primaryFrame().windowFrame.addWindowStateListener {
            FrameUpdater.updateFrames()
        }
    }

    private fun startRenderLoop() {
        glClearColor(1f, 1f, 1f, 1f)

        glEnable(GL_TEXTURE_2D)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        while (!Display.isCloseRequested()) {
            if (FrameUpdater.hasFramesToUpdate()) {
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                val width = Display.getWidth()
                val height = Display.getHeight()

                glViewport(0, 0, width, height)

                glMatrixMode(GL_PROJECTION)
                glLoadIdentity()
                glOrtho(0.0, width.toDouble(), 0.0, height.toDouble(), 1.0, -1.0)
                glMatrixMode(GL_MODELVIEW)
                glLoadIdentity()

                // actual map rendering
                renderCurrentMapIfExists()

                Display.update(false)
            }

            Display.processMessages()
            Display.sync(60)
        }
    }

    private fun renderCurrentMapIfExists() {
        if (currentMap == null) {
            return
        }

        val map = currentMap!!

        val horTilesNum = (Display.getWidth() / map.iconSize + 0.5f).toInt()
        val verTilesNum = (Display.getHeight() / map.iconSize + 0.5f).toInt()

        val frameRenderInstances = buildFrame(map, 0, 0, horTilesNum, verTilesNum)

        frameRenderInstances.values.forEach { plane ->
            plane.values.forEach { layer ->
                layer.forEach { ri ->
                    glColor4f(ri.color.red, ri.color.green, ri.color.blue, ri.color.alpha)
                    glBindTexture(GL_TEXTURE_2D, ri.textureId)

                    glPushMatrix()
                    glTranslatef(ri.x, ri.y, 0f)

                    glBegin(GL_QUADS)
                    with(ri) {
                        glTexCoord2f(0f, 1f)
                        glVertex2f(0f, 0f)

                        glTexCoord2f(1f, 1f)
                        glVertex2f(iconSize, 0f)

                        glTexCoord2f(1f, 0f)
                        glVertex2f(iconSize, iconSize)

                        glTexCoord2f(0f, 0f)
                        glVertex2f(0f, iconSize)
                    }
                    glEnd()

                    glPopMatrix()
                }
            }
        }
    }

    // Class to control the moment when we need to update OpenGL canvas.
    // Thus while we doesn't touch map canvas we won't spend any CPU to process stuff.
    private object FrameUpdater {

        private const val NO_FRAMES_TO_UPD = 0
        private const val DEFAULT_FRAMES_TO_UPD = 2 // To render everything properly we need 2 frames

        private var updateCounter = DEFAULT_FRAMES_TO_UPD

        fun updateFrames() {
            updateCounter = DEFAULT_FRAMES_TO_UPD
        }

        fun hasFramesToUpdate() = updateCounter-- != NO_FRAMES_TO_UPD
    }
}
