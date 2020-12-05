package strongdmm.service.screenshot

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30.*
import strongdmm.byond.dmm.MapArea
import strongdmm.service.frame.FrameMesh
import java.nio.ByteBuffer

class ScreenshotRenderer {
    lateinit var providedComposedFrame: List<FrameMesh>

    fun render(width: Int, height: Int, mapArea: MapArea, xShift: Float, yShift: Float): ByteBuffer {
        val frameBuffer = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer)

        val screenTexture = glGenTextures()

        glBindTexture(GL_TEXTURE_2D, screenTexture)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glBindTexture(GL_TEXTURE_2D, 0)

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, screenTexture, 0)

        glViewport(0, 0, width, height)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, width.toDouble(), 0.0, height.toDouble(), -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glLoadIdentity()
        glTranslatef(xShift, yShift, 0f)

        glClearColor(0f, 0f, 0f, 0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glEnable(GL_TEXTURE_2D)

        var currentTexture = -1

        for (frameMesh in providedComposedFrame) {
            if (!mapArea.isInBounds(frameMesh.mapX, frameMesh.mapY)) {
                continue
            }

            val sprite = frameMesh.sprite
            val colorR = frameMesh.colorR
            val colorG = frameMesh.colorG
            val colorB = frameMesh.colorB
            val colorA = frameMesh.colorA

            // More effectively would be to merge all textures into one atlas instead of such batching, but this is fine too.
            if (currentTexture != sprite.textureId) {
                if (currentTexture != -1) {
                    glEnd()
                }

                glBindTexture(GL_TEXTURE_2D, sprite.textureId)

                currentTexture = sprite.textureId
                glBegin(GL_QUADS)
            }

            glColor4f(colorR, colorG, colorB, colorA)

            glTexCoord2f(sprite.u2, sprite.v1)
            glVertex2i(frameMesh.x2, frameMesh.y2)
            glTexCoord2f(sprite.u1, sprite.v1)
            glVertex2i(frameMesh.x1, frameMesh.y2)
            glTexCoord2f(sprite.u1, sprite.v2)
            glVertex2i(frameMesh.x1, frameMesh.y1)
            glTexCoord2f(sprite.u2, sprite.v2)
            glVertex2i(frameMesh.x2, frameMesh.y1)
        }

        glEnd()

        glBindTexture(GL_TEXTURE_2D, 0)
        glDisable(GL_TEXTURE_2D)

        val imageBytes = BufferUtils.createByteBuffer(width * height * 4)

        glBindTexture(GL_TEXTURE_2D, screenTexture)
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBytes)
        glBindTexture(GL_TEXTURE_2D, 0)

        glDeleteTextures(screenTexture)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glDeleteFramebuffers(frameBuffer)

        return imageBytes
    }
}
