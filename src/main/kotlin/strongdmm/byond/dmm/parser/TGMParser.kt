package strongdmm.byond.dmm.parser

import kotlin.math.max

class TGMParser(
    private val rawMapContent: String
) {
    private val tilesRegex: Regex = "\\(\\d+,\\d+,(\\d+)\\).+?\"(.+?)\"".toRegex(RegexOption.DOT_MATCHES_ALL)

    private val tileContentsByKey: MutableMap<String, TileContent> = mutableMapOf()
    private var currentTileContent: TileContent = TileContent()
    private lateinit var currentTileObject: TileObject

    private val dmmData: DmmData = DmmData()

    fun parse(): DmmData {
        var posIdx = 0

        for (line in rawMapContent.lineSequence()) {
            posIdx += line.length + 1

            if (line.isBlank()) {
                break
            }

            readTileDeclaration(line)
        }

        tileContentsByKey.forEach { (key, tileContent) -> dmmData.addKeyAndTileContent(key, tileContent) }

        val tiles = readTiles(rawMapContent.substring(posIdx))

        dmmData.isTgm = true
        dmmData.keyLength = max(1, dmmData.keyLength)
        dmmData.setDmmSize(max(1, tiles.size), runCatching { tiles[0].size }.getOrDefault(1), runCatching { tiles[0][0].size }.getOrDefault(1))

        for (z in 1..dmmData.maxZ) {
            for (y in 1..dmmData.maxY) {
                for (x in 1..dmmData.maxX) {
                    dmmData.tiles[z - 1][y - 1][x - 1] = dmmData.getTileContentByKey(tiles[z - 1][y - 1][x - 1])!!
                }
            }
        }

        return dmmData
    }

    private fun readTileDeclaration(currentLine: String) {
        when (currentLine[0]) {
            '"' -> addNewTileContent(currentLine)
            '/' -> addNewTileObject(currentLine)
            '\t' -> {
                if (currentLine[1] != '}') {
                    addVarToCurrentObject(currentLine)
                }
            }
        }
    }

    private fun addNewTileContent(currentLine: String) {
        if (dmmData.keyLength == 0) {
            dmmData.keyLength = currentLine.indexOf('"', 1) - 1
        }

        val key = currentLine.substring(1, 1 + dmmData.keyLength)
        currentTileContent = TileContent()
        tileContentsByKey[key] = currentTileContent
    }

    private fun addNewTileObject(currentLine: String) {
        val type = currentLine.substring(0, currentLine.length - 1)
        currentTileObject = TileObject(type)
        currentTileContent.content.add(currentTileObject)
    }

    private fun addVarToCurrentObject(currentLine: String) {
        val varName = currentLine.substring(1, currentLine.indexOf(' '))

        val varValue: String = if (currentLine[currentLine.length - 1] == ';') {
            currentLine.substring(currentLine.indexOf(' ') + 3, currentLine.length - 1)
        } else {
            currentLine.substring(currentLine.indexOf(' ') + 3)
        }

        currentTileObject.putVar(varName, varValue)
    }

    private fun readTiles(rawTiles: String): MutableList<MutableList<MutableList<String>>> {
        val tilesGroupMatcher = tilesRegex.toPattern().matcher(rawTiles)
        val tiles = mutableListOf<MutableList<MutableList<String>>>()

        var currColumns = mutableListOf<MutableList<String>>()
        var currZ = 1

        fun addToGroupedTiles() {
            val groupedTilesColumns = mutableListOf<MutableList<String>>()

            for (y in 0 until currColumns[0].size) {
                val row = mutableListOf<String>()

                for (x in 0 until currColumns.size) {
                    row.add(currColumns[x][y])
                }

                groupedTilesColumns.add(row)
            }

            currColumns = mutableListOf()
            tiles.add(groupedTilesColumns.asReversed())
        }

        while (tilesGroupMatcher.find()) {
            val z = tilesGroupMatcher.group(1).toInt()

            if (z != currZ) {
                addToGroupedTiles()
                currZ = z
            }

            val column = mutableListOf<String>()

            for (row in tilesGroupMatcher.group(2).trim().lines()) {
                val key = row.trim()
                if (key.isNotEmpty()) {
                    column.add(key)
                }
            }

            currColumns.add(column)
        }

        addToGroupedTiles()

        return tiles
    }
}
