package io.github.spair.strongdmm.logic.dmi

import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.*
import javax.swing.Icon

class IconSprite(private val dmi: Dmi, index: Int) : Icon {

    // Classic icon position for top-down coordinate system
    var x1: Int
    var y1: Int
    var x2: Int
    var y2: Int

    // UV mapping (used by OpenGL)
    val u1: Float
    val v1: Float
    val u2: Float
    val v2: Float

    val scaledIcon: ScaledIcon by lazy { ScaledIcon() }

    init {
        val x = index % dmi.cols
        val y = index / dmi.cols

        x1 = x * dmi.spriteWidth
        y1 = y * dmi.spriteHeight
        x2 = (x + 1) * dmi.spriteWidth
        y2 = (y + 1) * dmi.spriteHeight

        u1 = x / dmi.cols.toFloat() + UV_MARGIN
        v1 = y / dmi.rows.toFloat() + UV_MARGIN
        u2 = (x + 1) / dmi.cols.toFloat() - UV_MARGIN
        v2 = (y + 1) / dmi.rows.toFloat() - UV_MARGIN
    }

    override fun paintIcon(c: Component, g: Graphics, px: Int, py: Int) {
        g.drawImage(dmi.atlas, px, py, px + dmi.spriteWidth, py + dmi.spriteHeight, x1, y1, x2, y2, c)
    }

    override fun getIconWidth() = dmi.spriteWidth
    override fun getIconHeight() = dmi.spriteHeight

    fun isOpaquePixel(x: Int, y: Int) = dmi.atlas.getRGB(x1 + x, y1 + y) shr 24 != 0x00

    inner class ScaledIcon(private val scaledSize: Int = 16) : Icon {
        override fun paintIcon(c: Component, g: Graphics, px: Int, py: Int) {
            (g as Graphics2D).run {
                setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR)
                setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF)
                setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED)
                drawImage(dmi.atlas, px, py, px + scaledSize, py + scaledSize, x1, y1, x2, y2, c)
            }
        }

        override fun getIconWidth(): Int = scaledSize
        override fun getIconHeight(): Int = scaledSize
    }

    companion object {
        // This margin expands sprite during rendering, so while zooming no gaps between sprites appear.
        private const val UV_MARGIN = .000001f
    }
}
