package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.common.ViewController
import io.github.spair.strongdmm.logic.input.InputProcessor
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.render.FrameRenderer
import org.kodein.di.direct
import org.kodein.di.erased.instance
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import kotlin.concurrent.thread

class MapCanvasController : ViewController<MapCanvasView>(DI.direct.instance()) {

    private val frameRenderer by DI.instance<FrameRenderer>()
    private val inputProcessor by lazy { InputProcessor(this) }

    private var selectedMap: Dmm? = null
    private var glInitialized = false

    var xViewOffset = 0f
    var yViewOffset = 0f

    var xMapOff = 0
    var yMapOff = 0

    override fun init() {
    }

    fun selectMap(dmm: Dmm) {
        selectedMap = dmm

        if (!glInitialized) {
            initGLDisplay()
        }
    }

    fun updateMapOffset() {
        xMapOff = -xViewOffset.toInt() / selectedMap!!.iconSize
        yMapOff = -yViewOffset.toInt() / selectedMap!!.iconSize
    }

    private fun initGLDisplay() {
        thread(start = true) {
            glInitialized = true
            Display.setParent(view.canvas)
            Display.create()
            startRenderLoop()
            Display.destroy()
            glInitialized = false
        }
    }

    private fun startRenderLoop() {
        glClearColor(1f, 1f, 1f, 1f)

        glEnable(GL_TEXTURE_2D)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        while (!Display.isCloseRequested() && selectedMap != null) {
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            val width = Display.getWidth()
            val height = Display.getHeight()

            glViewport(0, 0, width, height)

            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            glOrtho(0.0, width.toDouble(), 0.0, height.toDouble(), 1.0, -1.0)
            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()
            glTranslatef(xViewOffset, yViewOffset, 0f)

            // actual map rendering
            renderSelectedMap()

            Display.update()
            inputProcessor.fire()
            Display.sync(60)
        }
    }

    private fun renderSelectedMap() {
        val map = selectedMap!!

        val horTilesNum = (Display.getWidth() / map.iconSize + 0.5f).toInt()
        val verTilesNum = (Display.getHeight() / map.iconSize + 0.5f).toInt()

        val frameRenderInstances = frameRenderer.buildFrame(map, xMapOff, yMapOff, horTilesNum, verTilesNum)

        frameRenderInstances.values.forEach { plane ->
            plane.values.forEach { layer ->
                layer.forEach { ri ->
                    glColor4f(ri.color.red, ri.color.green, ri.color.blue, ri.color.alpha)
                    glBindTexture(GL_TEXTURE_2D, ri.textureId)

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
    }
}
