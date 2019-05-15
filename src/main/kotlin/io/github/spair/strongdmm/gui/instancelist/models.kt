package io.github.spair.strongdmm.gui.instancelist

import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dmi.SOUTH
import io.github.spair.strongdmm.logic.map.TileItem

data class ItemInstance(
    val name: String,
    val icon: String,
    val iconState: String,
    val type: String,
    val dir: Int = SOUTH,
    val customVars: Map<String, String> = emptyMap()
) {

    override fun toString() = name

    fun toTileItem(x: Int, y: Int) = TileItem(Environment.dme.getItem(type)!!, x, y, customVars.toMutableMap())
}
