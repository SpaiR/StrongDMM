package strongdmm.byond.dmm.parser

class DmmData {
    var isTgm: Boolean = false

    var keyLength: Int = 0

    var maxX: Int = 0
    var maxY: Int = 0
    var maxZ: Int = 0

    lateinit var tiles: Array<Array<Array<TileContent?>>>

    private val tileContentsByKey: MutableMap<String, TileContent> = mutableMapOf()
    private val keysByTileContent: MutableMap<TileContent, String> = mutableMapOf()

    val keys: List<String>
        get() = tileContentsByKey.keys.sortedWith(TileKeyComparator())

    fun setDmmSize(maxZ: Int, maxY: Int, maxX: Int) {
        this.maxZ = maxZ
        this.maxY = maxY
        this.maxX = maxX

        tiles = Array(maxZ) { Array(maxY) { Array<TileContent?>(maxX) { null } } }
    }

    fun addKeyAndTileContent(key: String, tileContent: TileContent) {
        tileContentsByKey[key] = tileContent
        keysByTileContent[tileContent] = key
    }

    fun getTileContentByLocation(x: Int, y: Int, z: Int): TileContent? = tiles[z - 1][y - 1][x - 1]

    fun getTileContentByKey(key: String?): TileContent? = tileContentsByKey[key]

    fun getKeyByTileContent(tileContent: TileContent?): String? = keysByTileContent[tileContent]

    fun getKeyByLocation(x: Int, y: Int, z: Int): String? = getKeyByTileContent(getTileContentByLocation(x, y, z))

    fun hasTileContentByKey(key: String): Boolean = tileContentsByKey.containsKey(key)

    fun hasKeyByTileContent(tileContent: TileContent): Boolean = keysByTileContent.containsKey(tileContent)

    fun addTileContentByLocation(x: Int, y: Int, z: Int, tileContent: TileContent) {
        tiles[z - 1][y - 1][x - 1] = tileContent
    }

    fun hasLocationsWithoutContent(): Boolean {
        for (z in 1..maxZ) {
            for (y in 1..maxY) {
                for (x in 1..maxX) {
                    if (getTileContentByLocation(x, y, z) == null) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun removeKeyAndTileContent(key: String?) {
        keysByTileContent.remove(tileContentsByKey.remove(key))
    }
}
