package io.github.spair.strongdmm.logic.map

import io.github.spair.byond.ByondTypes
import io.github.spair.byond.ByondVars
import io.github.spair.byond.VarWrapper
import io.github.spair.byond.dme.Dme
import io.github.spair.byond.dme.DmeItem
import io.github.spair.dmm.io.DmmData
import io.github.spair.dmm.io.TileLocation

class Dmm(dmmData: DmmData, dme: Dme) {

    val maxX: Int
    val maxY: Int
    val iconSize: Int

    private val tiles: Array<Array<Tile?>>

    init {
        maxX = dmmData.maxX
        maxY = dmmData.maxY
        iconSize = dme.getItem(ByondTypes.WORLD).getVarIntSafe(ByondVars.ICON_SIZE).orElse(32)
        tiles = Array(maxY) { arrayOfNulls<Tile>(maxX) }

        for (x in 1..maxX) {
            for (y in maxY downTo 1) {
                val tileItems = mutableListOf<TileItem>()

                for (tileContent in dmmData.getTileContentByLocation(TileLocation.of(x, y))) {
                    val dmeItem = dme.getItem(tileContent.type)
                    if (dmeItem != null) {
                        tileItems.add(
                            TileItem(
                                dme.getItem(tileContent.type),
                                x,
                                y,
                                tileContent.vars
                            )
                        )
                    }
                }

                tiles[y - 1][x - 1] = Tile(x, y, tileItems.toList())
            }
        }
    }

    fun getTile(x: Int, y: Int) = if (x in 1..maxX && y in 1..maxY) {
        tiles[y - 1][x - 1]
    } else {
        null
    }
}

class Tile(val x: Int, val y: Int, val tileItems: List<TileItem>) : Iterable<TileItem> {
    override fun iterator(): Iterator<TileItem> {
        return tileItems.iterator()
    }
}

class TileItem(private val dmeItem: DmeItem, val x: Int, val y: Int, private val customVars: Map<String, String>) {

    val type: String

    init {
        this.type = dmeItem.type
    }

    fun isType(type: String) = dmeItem.isType(type)

    fun getVar(name: String) = VarWrapper.rawValue(customVars.getOrDefault(name, dmeItem.getVar(name)))

    fun getVarText(name: String) = getVarTextSafe(name).orElse(null)

    fun getVarTextSafe(name: String) = VarWrapper.optionalText(customVars.getOrDefault(name, dmeItem.getVar(name)))

    fun getVarFilePath(name: String) = getVarFilePathSafe(name).orElse(null)

    fun getVarFilePathSafe(name: String) =
        VarWrapper.optionalFilePath(customVars.getOrDefault(name, dmeItem.getVar(name)))

    fun getVarInt(name: String) = getVarIntSafe(name).orElse(null)

    fun getVarIntSafe(name: String) = VarWrapper.optionalInt(customVars.getOrDefault(name, dmeItem.getVar(name)))

    fun getVarDouble(name: String) = getVarDoubleSafe(name).orElse(null)

    fun getVarDoubleSafe(name: String) = VarWrapper.optionalDouble(customVars.getOrDefault(name, dmeItem.getVar(name)))

    fun getVarBool(name: String) = getVarBoolSafe(name).orElse(null)

    fun getVarBoolSafe(name: String) = VarWrapper.optionalBoolean(customVars.getOrDefault(name, dmeItem.getVar(name)))
}
