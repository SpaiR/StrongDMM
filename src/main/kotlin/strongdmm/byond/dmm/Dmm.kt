package strongdmm.byond.dmm

import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.parser.DmmData
import strongdmm.byond.dmm.parser.TileContent
import java.io.File
import java.nio.file.Paths

class Dmm(
    mapFile: File,
    initialDmmData: DmmData,
    dme: Dme
) {
    companion object {
        const val MAP_ID_NONE: Int = -1
    }

    val basicAreaType: String = dme.basicAreaType
    val basicTurfType: String = dme.basicTurfType

    val id: Int = mapFile.absolutePath.hashCode()
    val mapName: String = mapFile.nameWithoutExtension

    val mapPath: MapPath = MapPath(Paths.get(dme.absRootDirPath).relativize(mapFile.toPath()).toString(), mapFile.absolutePath)

    var maxX: Int = initialDmmData.maxX
        private set
    var maxY: Int = initialDmmData.maxY
        private set
    var maxZ: Int = initialDmmData.maxZ
        private set

    var zActive: Int = 1

    private var tiles: Array<Array<Array<LongArray>>> = Array(maxZ) { Array(maxY) { Array(maxX) { LongArray(0) } } }

    init {
        for (z in 1..maxZ) {
            for (y in 1..maxY) {
                for (x in 1..maxX) {
                    var tileItems = LongArray(0)

                    initialDmmData.getTileContentByLocation(x, y, z)?.let { tileContent ->
                        for (tileObject in tileContent.content) {
                            if (dme.getItem(tileObject.type) != null) {
                                tileItems += GlobalTileItemHolder.getOrCreate(tileObject.type, tileObject.vars).id
                            }
                        }
                    }

                    tiles[z - 1][y - 1][x - 1] = tileItems
                }
            }
        }
    }

    fun getTileContentByLocation(x: Int, y: Int, z: Int): TileContent {
        return getTile(x, y, z).getTileContent()
    }

    fun getTile(x: Int, y: Int, z: Int): Tile = Tile(this, x, y, z)
    fun getTileItemsId(x: Int, y: Int, z: Int): LongArray = tiles[z - 1][y - 1][x - 1]

    fun setTileItemsId(x: Int, y: Int, z: Int, tileItemsId: LongArray) {
        tiles[z - 1][y - 1][x - 1] = tileItemsId
    }

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
