package strongdmm.byond.dmm.parser

class ByondParser(
    private val rawMapContent: String
) {
    private val objsRegex: Regex = "(,|^)(?=/)(?![^{]*[}])".toRegex()
    private val objWithVarRegex: Regex = "^(/.+)\\{(.*)}".toRegex()
    private val varsRegex: Regex = ";[ ]?(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()
    private val tilesRegex: Regex = "\\(\\d+,\\d+,\\d+\\).+?\"(.+?)\"".toRegex(RegexOption.DOT_MATCHES_ALL)

    private lateinit var keySplit: Regex

    private val dmmData: DmmData = DmmData()

    fun parse(): DmmData {
        var posIdx = 0

        for (line in rawMapContent.lineSequence()) {
            posIdx += line.length + 1

            if (line.isBlank()) {
                break
            }

            readTileContentDeclaration(line)
        }

        val tiles = readTiles(rawMapContent.substring(posIdx))

        dmmData.setDmmSize(tiles.size, tiles[0].size, tiles[0][0].size)

        for (z in 1..dmmData.maxZ) {
            for (y in 1..dmmData.maxY) {
                for (x in 1..dmmData.maxX) {
                    dmmData.tiles[z - 1][y - 1][x - 1] = dmmData.getTileContentByKey(tiles[z - 1][y - 1][x - 1])!!
                }
            }
        }

        return dmmData
    }

    private fun readTileContentDeclaration(currentLine: String) {
        if (dmmData.keyLength == 0) {
            dmmData.keyLength = currentLine.indexOf('"', 1) - 1
            keySplit = "(?<=\\G.{${dmmData.keyLength}})".toRegex()
        }

        val key = currentLine.substring(1, 1 + dmmData.keyLength)
        val rawValue = currentLine.substring(currentLine.indexOf('('))
        val tileContent = readTileContent(rawValue.substring(1, rawValue.length - 1))

        dmmData.addKeyAndTileContent(key, tileContent)
    }

    private fun readTileContent(rawContent: String): TileContent {
        val allObjects = objsRegex.split(rawContent.trim()).filter { it.isNotBlank() }
        val tileContent = mutableListOf<TileObject>()

        for (item in allObjects) {
            var type: String
            val vars = mutableMapOf<String, String>()

            val itemWithVar = objWithVarRegex.toPattern().matcher(item)

            if (itemWithVar.find()) {
                type = itemWithVar.group(1)

                for (varDef in varsRegex.split(itemWithVar.group(2))) {
                    val trimmedVarDef = varDef.trim { it <= ' ' }

                    val varName = trimmedVarDef.substring(0, trimmedVarDef.indexOf(' '))
                    val varValue = trimmedVarDef.substring(trimmedVarDef.indexOf(' ') + 3)

                    vars[varName] = varValue
                }
            } else {
                type = item
            }

            val tileObject = TileObject(type)
            tileObject.setVars(if (vars.isEmpty()) null else vars)
            tileContent.add(tileObject)
        }

        return TileContent().apply { content.addAll(tileContent) }
    }

    private fun readTiles(rawTiles: String): MutableList<MutableList<MutableList<String>>> {
        val tilesGroupMatcher = tilesRegex.toPattern().matcher(rawTiles)
        val tiles = mutableListOf<MutableList<MutableList<String>>>()

        while (tilesGroupMatcher.find()) {
            val rows = mutableListOf<MutableList<String>>()

            for (row in tilesGroupMatcher.group(1).trim().lines()) {
                val keys = mutableListOf<String>()
                keys.addAll(row.trim().split(keySplit).filter { it.isNotBlank() })
                rows.add(keys)
            }

            tiles.add(rows.asReversed()) // we parse from top to bottom while the map content goes from bottom to top
        }

        return tiles
    }
}
