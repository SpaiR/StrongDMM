package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.map.TileItem
import javax.imageio.ImageIO

private val PLACEHOLDER_TEXTURE_ID: Int by lazy {
    createGlTexture(ImageIO.read(Environment::class.java.classLoader.getResource("placeholder.png")))
}

fun createRenderInstance(initialX: Int, initialY: Int, tileItem: TileItem): RenderInstance {
    return RenderInstance(
        initialX.toFloat(),
        initialY.toFloat(),
        PLACEHOLDER_TEXTURE_ID
    )  // TODO: replace with actual logic
}

data class RenderInstance(
    val x: Float,
    val y: Float,
    val textureId: Int,
    val color: Color = Color(1f, 1f, 1f, 1f),
    val iconSize: Float = 32f
)

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float)
