package io.github.spair.strongdmm.logic.render

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12
import java.awt.image.BufferedImage

fun createGlTexture(img: BufferedImage): Int {
    val pixels = img.getRGB(0, 0, img.width, img.height, null, 0, img.width)

    val buffer = BufferUtils.createByteBuffer(img.width * img.height * 4).apply {
        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                val pixel = pixels[y * img.width + x]
                put((pixel shr 16 and 0xFF).toByte()) // Red
                put((pixel shr 8 and 0xFF).toByte())  // Green
                put((pixel and 0xFF).toByte())        // Blue
                put((pixel shr 24 and 0xFF).toByte()) // Alpha
            }
        }

        flip()
    }

    val textureId = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, textureId)

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.width, img.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

    return textureId
}
