package io.github.spair.strongdmm.logic.map

import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import java.awt.Canvas
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import kotlin.concurrent.thread

class MapDrawerGL(canvas: Canvas) {

    private var redraw = true
    private var currentMap: Dmm? = null

    init {
        thread(start = true) {
            Display.setParent(canvas)
            Display.create()

            canvas.addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    redraw = true
                }
            })

            startRenderLoop()
        }
    }

    fun switchMap(dmm: Dmm) {
        currentMap = dmm
    }

    private fun startRenderLoop() {
        glClearColor(1f, 0f, 0f, 1f)

        while (!Display.isCloseRequested()) {
            if (redraw) {
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                val width = Display.getWidth()
                val height = Display.getHeight()

                //glViewport(0, 0, width, height)

                glMatrixMode(GL_PROJECTION)
                glLoadIdentity()
                glOrtho(0.0, width.toDouble(), 0.0, height.toDouble(), 1.0, -1.0)
                glMatrixMode(GL_MODELVIEW)
                glLoadIdentity()

                glEnable(GL_TEXTURE_2D)

                glEnable(GL_BLEND)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

                Display.update(false)
                redraw = false
            }

            Display.processMessages()
            Display.sync(60)
        }

        Display.destroy()
    }
}
