package io.github.spair.strongdmm.logic.dmi

import io.github.spair.strongdmm.common.*

class IconState(val name: String, val dirs: Int, val frames: Int, val sprites: List<IconSprite>) {

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
