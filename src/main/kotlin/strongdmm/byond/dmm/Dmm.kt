package strongdmm.byond.dmm

import io.github.spair.dmm.io.DmmData
import strongdmm.byond.dme.Dme
import java.io.File
import java.nio.file.Path

class Dmm(
    mapFile: File,
    initialDmmData: DmmData,
    dme: Dme
) {
    val mapName: String = mapFile.nameWithoutExtension
    val relativeMapPath: String = Path.of(dme.rootPath).relativize(mapFile.toPath()).toString()
    val id: Int = mapFile.absolutePath.hashCode()

    var maxX: Int = initialDmmData.maxX
        private set
    var maxY: Int = initialDmmData.maxY
        private set

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

    fun getTile(x: Int, y: Int): Tile = Tile(this, x, y)
    fun getTileItemsID(x: Int, y: Int): IntArray = tiles[y - 1][x - 1]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dmm

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}
