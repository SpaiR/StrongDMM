package io.github.spair.strongdmm.logic.dmi

import io.github.spair.strongdmm.logic.render.createGlTexture
import java.awt.image.BufferedImage

class Dmi(
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
