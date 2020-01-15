package strongdmm.byond.dmi

import strongdmm.byond.*

class IconState(
    val dirs: Int,
    val frames: Int,
    val sprites: List<IconSprite>
) {
    fun getIconSprite(): IconSprite = getIconSprite(DEFAULT_DIR)
    fun getIconSprite(dir: Int): IconSprite = getIconSprite(dir, 0)
    fun getIconSprite(dir: Int, frame: Int): IconSprite = sprites[dirToIndex(dir) + (frame % frames * dirs)]

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
