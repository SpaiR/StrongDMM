package io.github.spair.strongdmm.logic.map

import io.github.spair.dmm.io.DmmData
import io.github.spair.dmm.io.TileLocation
import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.dmi.SOUTH

class Dmm(dmmData: DmmData, dme: Dme) {

    val maxX: Int = dmmData.maxX
    val maxY: Int = dmmData.maxY
    val iconSize: Int = dme.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: 32

    private val tiles: Array<Array<Tile?>>

    init {
        tiles = Array(maxY) { arrayOfNulls<Tile>(maxX) }

        for (x in 1..maxX) {
            for (y in 1..maxY) {
                val tileItems = mutableListOf<TileItem>()

                for (tileContent in dmmData.getTileContentByLocation(TileLocation.of(x, y))) {
                    dme.getItem(tileContent.type)?.let {
                        tileItems.add(TileItem(dme.getItem(tileContent.type)!!, x, y, tileContent.vars.toSortedMap()))
                    }
                }

                tiles[y - 1][x - 1] = Tile(x, y, tileItems.toList())
            }
        }
    }

    fun getTile(x: Int, y: Int) = if (x in 1..maxX && y in 1..maxY) tiles[y - 1][x - 1] else null
}

class Tile(val x: Int, val y: Int, val tileItems: List<TileItem>) : Iterable<TileItem> {
    override fun iterator(): Iterator<TileItem> {
        return tileItems.iterator()
    }
}

class TileItem(val dmeItem: DmeItem, val x: Int, val y: Int, val customVars: Map<String, String>) {

    val type: String = dmeItem.type

    // Vars extensively used during rendering
    val icon = getVarText(VAR_ICON) ?: ""
    val iconState = getVarText(VAR_ICON_STATE) ?: ""
    val alpha = getVarInt(VAR_ALPHA) ?: 255
    val plane = getVarFloat(VAR_PLANE) ?: 0f
    val layer = getVarFloat(VAR_LAYER) ?: 0f
    val pixelX = getVarInt(VAR_PIXEL_X) ?: 0
    val pixelY = getVarInt(VAR_PIXEL_Y) ?: 0
    val dir = getVarInt(VAR_DIR) ?: SOUTH
    val color = getVarText(VAR_COLOR) ?: ""

    fun isType(type: String) = dmeItem.isType(type)

    fun getVar(name: String): String? = customVars[name] ?: dmeItem.getVar(name)
    fun getVarText(name: String): String? = customVars[name]?.run { substring(1, length - 1) } ?: dmeItem.getVarText(name)
    fun getVarInt(name: String): Int? = customVars[name]?.toIntOrNull() ?: dmeItem.getVarInt(name)
    fun getVarFloat(name: String): Float? = customVars[name]?.toFloatOrNull() ?: dmeItem.getVarFloat(name)
}
