package strongdmm.byond.dmm

import io.github.spair.dmm.io.DmmData
import strongdmm.byond.dme.Dme
import java.io.File

class Dmm(
    mapFile: File,
    initialDmmData: DmmData,
    dme: Dme
) {
    val mapName: String = mapFile.nameWithoutExtension
    val mapPath: String = mapFile.path

    private var maxX: Int = initialDmmData.maxX
    private var maxY: Int = initialDmmData.maxY

    private var tiles: Array<Array<IntArray>> = Array(maxY) { Array(maxX) { IntArray(0) } }

    init {
        for (x in 1..maxX) {
            for (y in 1..maxY) {
                var tileItems = IntArray(0)

                initialDmmData.getTileContentByLocation(x, y)?.let {
                    for (tileObject in it) {
                        if (dme.getItem(tileObject.type) != null) {
                            tileItems += GlobalTileItemHolder.getOrCreate(tileObject.type, tileObject.vars).id
                        }
                    }
                }

                tiles[y - 1][x - 1] = tileItems
            }
        }
    }

    fun getTileItems(x: Int, y: Int): IntArray = tiles[y - 1][x - 1]

    fun getMaxX(): Int = maxX
    fun getMaxY(): Int = maxY
}
