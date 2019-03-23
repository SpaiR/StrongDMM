package io.github.spair.strongdmm.logic.map

import io.github.spair.byond.ByondTypes
import io.github.spair.byond.ByondVars
import io.github.spair.byond.VarWrapper
import io.github.spair.byond.dme.Dme
import io.github.spair.byond.dme.DmeItem
import io.github.spair.dmm.io.DmmData
import io.github.spair.dmm.io.TileLocation

class Dmm(dmmData: DmmData, dme: Dme) {

    val maxX: Int = dmmData.maxX
    val maxY: Int = dmmData.maxY
    val iconSize: Int = dme.getItem(ByondTypes.WORLD).getVarIntSafe(ByondVars.ICON_SIZE).orElse(32)

    private val tiles: Array<Array<Tile?>>

    init {
        tiles = Array(maxY) { arrayOfNulls<Tile>(maxX) }

        for (x in 1..maxX) {
            for (y in maxY downTo 1) {
                val tileItems = mutableListOf<TileItem>()

                for (tileContent in dmmData.getTileContentByLocation(TileLocation.of(x, y))) {
                    val dmeItem = dme.getItem(tileContent.type)
                    if (dmeItem != null) {
                        tileItems.add(
                            TileItem(dme.getItem(tileContent.type), x, y, tileContent.vars)
                        )
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

class TileItem(private val dmeItem: DmeItem, val x: Int, val y: Int, val customVars: Map<String, String>) {

    val type: String = dmeItem.type
    val initialVars by lazy { dmeItem.allVars }

    fun isType(type: String) = dmeItem.isType(type)

    fun getVar(name: String): String? = initialVars[name] ?: initialVars[name]
    fun getVarText(name: String): String? = VarWrapper.optionalText(customVars[name]).orElse(dmeItem.getVarTextSafe(name).orElse(null))
    fun getVarPath(name: String): String? = VarWrapper.optionalFilePath(customVars[name]).orElse(dmeItem.getVarFilePathSafe(name).orElse(null))
    fun getVarInt(name: String): Int? = VarWrapper.optionalInt(customVars[name]).orElse(dmeItem.getVarIntSafe(name).orElse(null))
    fun getVarDouble(name: String): Double? = VarWrapper.optionalDouble(customVars[name]).orElse(dmeItem.getVarDoubleSafe(name).orElse(null))
    fun getVarBool(name: String): Boolean? = VarWrapper.optionalBoolean(customVars[name]).orElse(dmeItem.getVarBoolSafe(name).orElse(null))
}
