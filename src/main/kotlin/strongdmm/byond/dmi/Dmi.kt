package strongdmm.byond.dmi

class Dmi(
    val spriteWidth: Int,
    val spriteHeight: Int,
    val rows: Int,
    val cols: Int,
    val textureId: Int,
    val iconStates: Map<String, IconState>
) {
    val textureWidth: Int = spriteWidth * cols
    val textureHeight: Int = spriteHeight * rows

    fun getIconState(iconState: String): IconState? = iconStates[iconState] ?: iconStates[""]
}
