package io.github.spair.strongdmm.logic.dmi

import io.github.spair.strongdmm.logic.render.createGlTexture
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.*
import java.awt.image.BufferedImage
import javax.swing.Icon

const val NORTH = 1
const val SOUTH = 2
const val EAST = 4
const val WEST = 8

const val NORTHEAST = 5
const val NORTHWEST = 9
const val SOUTHEAST = 6
const val SOUTHWEST = 10

data class Dmi(
    val atlas: BufferedImage,
    val spriteWidth: Int,
    val spriteHeight: Int,
    val rows: Int,
    val cols: Int,
    private val iconStates: Map<String, IconState>
) {
    val glTextureId by lazy { createGlTexture(atlas) }

    fun getIconState(iconState: String) = iconStates[iconState] ?: iconStates[""]
}

data class IconState(val name: String, val dirs: Int, val frames: Int, val sprites: List<IconSprite>) {

    fun getIconSprite() = getIconSprite(SOUTH)
    fun getIconSprite(dir: Int) = getIconSprite(dir, 0)
    fun getIconSprite(dir: Int, frame: Int) = sprites[dirToIndex(dir) + (frame % frames * dirs)]

    private fun dirToIndex(dir: Int): Int {
        if (dirs == 1 || dir < NORTH || dir > SOUTHWEST) {
            return 0
        }

        val index = when (dir) {
            SOUTH -> 0
            NORTH -> 1
            EAST -> 2
            WEST -> 3
            SOUTHEAST -> 4
            SOUTHWEST -> 5
            NORTHEAST -> 6
            NORTHWEST -> 7
            else -> 0
        }

        return if (index + 1 <= sprites.size) index else 0
    }
}

// This margin expands sprite during rendering,
// so while zooming no gaps between sprites appear.
private const val UV_MARGIN = .000001f

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
}
