package strongdmm.byond.dmi

class Dmi(
    val spriteWidth: Int,
    val spriteHeight: Int,
    val rows: Int,
    val cols: Int,
    val textureId: Int,
    private val iconStates: Map<String, IconState>
) {
    fun getIconState(iconState: String): IconState? = iconStates[iconState] ?: iconStates[""]
}
