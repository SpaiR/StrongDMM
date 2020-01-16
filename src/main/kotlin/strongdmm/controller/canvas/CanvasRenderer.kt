package strongdmm.controller.canvas

import org.lwjgl.opengl.GL30.*
import strongdmm.controller.frame.FrameMesh
import strongdmm.util.OUT_OF_BOUNDS

class CanvasRenderer {
    var redraw: Boolean = false

    private val frameBuffer: Int = glGenFramebuffers()
    private var isTextureAttached: Boolean = false

    private var canvasTexture: Int = -1
    private var canvasTextureIsFilled: Boolean = false

    var windowWidth: Int = -1
    var windowHeight: Int = -1

    fun render(
        frameMeshes: List<FrameMesh>,
        windowWidth: Int,
        windowHeight: Int,
        renderData: RenderData,
        xMapMousePos: Int,
        yMapMousePos: Int,
        iconSize: Int
    ) {
        if (windowWidth == 0 && windowHeight == 0) {
            return
        }

        if (this.windowWidth != windowWidth || this.windowHeight != windowHeight || canvasTexture == -1) {
            this.windowWidth = windowWidth
            this.windowHeight = windowHeight
            createCanvasTexture()
        }

        if (canvasTextureIsFilled) {
            glViewport(0, 0, windowWidth, windowHeight)
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            glOrtho(0.0, windowWidth.toDouble(), 0.0, windowHeight.toDouble(), -1.0, 1.0)
            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()

            renderCanvasTexture()
            renderMousePosition(renderData, xMapMousePos, yMapMousePos, iconSize)

            if (!redraw) {
                return
            }
        }

        fillCanvasTexture(renderData, frameMeshes)

        canvasTextureIsFilled = true
        redraw = false
    }

    fun invalidateCanvasTexture() {
        if (canvasTexture != -1) {
            glDeleteTextures(canvasTexture)
            canvasTexture = -1
            canvasTextureIsFilled = false
            isTextureAttached = false
        }
    }

    private fun renderCanvasTexture() {
        glColor4f(1f, 1f, 1f, 1f)

        glEnable(GL_TEXTURE_2D)
        glBindTexture(GL_TEXTURE_2D, canvasTexture)

        glBegin(GL_QUADS)
        glTexCoord2i(0, 0)
        glVertex2i(0, 0)
        glTexCoord2i(1, 0)
        glVertex2i(windowWidth, 0)
        glTexCoord2i(1, 1)
        glVertex2i(windowWidth, windowHeight)
        glTexCoord2i(0, 1)
        glVertex2i(0, windowHeight)
        glEnd()

        glBindTexture(GL_TEXTURE_2D, 0)
        glDisable(GL_TEXTURE_2D)
    }

    private fun renderMousePosition(renderData: RenderData, xMapMousePos: Int, yMapMousePos: Int, iconSize: Int) {
        if (xMapMousePos != OUT_OF_BOUNDS && yMapMousePos != OUT_OF_BOUNDS) {
            val xPos = ((xMapMousePos - 1) * iconSize + renderData.viewTranslateX) / renderData.viewScale
            val yPos = ((yMapMousePos - 1) * iconSize + renderData.viewTranslateY) / renderData.viewScale
            val realIconSize = iconSize / renderData.viewScale

            glColor4f(1f, 1f, 1f, 0.25f)

            glBegin(GL_QUADS)
            glVertex2d(xPos, yPos)
            glVertex2d(xPos + realIconSize, yPos)
            glVertex2d(xPos + realIconSize, yPos + realIconSize)
            glVertex2d(xPos, yPos + realIconSize)
            glEnd()
        }
    }

    // Method will render the map to the separate texture which will be reused later to avoid CPU usage while idle.
    private fun fillCanvasTexture(renderData: RenderData, frameMeshes: List<FrameMesh>) {
        val viewWidthWithScale = windowWidth * renderData.viewScale
        val viewHeightWithScale = windowHeight * renderData.viewScale

        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer)

        if (!isTextureAttached) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, canvasTexture, 0)
            isTextureAttached = true
        }

        glViewport(0, 0, windowWidth, windowHeight)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, viewWidthWithScale, 0.0, viewHeightWithScale, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glLoadIdentity()

        glClearColor(.25f, .25f, .5f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        glEnable(GL_TEXTURE_2D)

        var currentTexture = -1

        for (frameMesh in frameMeshes) {
            val (sprite, x1, y1, x2, y2, color) = frameMesh

            val rx1 = x1 + renderData.viewTranslateX
            val ry1 = y1 + renderData.viewTranslateY
            val rx2 = x2 + renderData.viewTranslateX
            val ry2 = y2 + renderData.viewTranslateY

            if (viewWidthWithScale < rx1 || viewHeightWithScale < ry1 || rx2 < 0 || ry2 < 0) {
                continue
            }

            // More effectively will be to merge all textures into one atlas instead of such batching, but this is fine too.
            if (currentTexture != sprite.textureId) {
                if (currentTexture != -1) {
                    glEnd()
                }

                glBindTexture(GL_TEXTURE_2D, sprite.textureId)

                currentTexture = sprite.textureId
                glBegin(GL_QUADS)
            }

            glColor4f(color.red, color.green, color.blue, color.alpha)

            glTexCoord2f(sprite.u2, sprite.v1)
            glVertex2d(rx2, ry2)
            glTexCoord2f(sprite.u1, sprite.v1)
            glVertex2d(rx1, ry2)
            glTexCoord2f(sprite.u1, sprite.v2)
            glVertex2d(rx1, ry1)
            glTexCoord2f(sprite.u2, sprite.v2)
            glVertex2d(rx2, ry1)
        }

        glEnd()
        glBindTexture(GL_TEXTURE_2D, 0)

        glDisable(GL_TEXTURE_2D)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    private fun createCanvasTexture() {
        invalidateCanvasTexture()
        canvasTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, canvasTexture)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, windowWidth, windowHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}
