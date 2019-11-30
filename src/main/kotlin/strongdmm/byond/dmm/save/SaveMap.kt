package strongdmm.byond.dmm.save

import gnu.trove.list.array.TLongArrayList
import io.github.spair.dmm.io.DmmData
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.TileItemIdx
import java.io.File

class SaveMap(
    private val dmm: Dmm,
    private val initialDmmData: DmmData,
    isTgm: Boolean
) {
    private val outputDmmData: DmmData = DmmData().apply {
        setDmmSize(initialDmmData.maxX, initialDmmData.maxY)
        keyLength = initialDmmData.keyLength
        this.isTgm = isTgm
    }

    private val keyGenerator: KeyGenerator = KeyGenerator(outputDmmData)
    private val unusedKeys: MutableSet<String> = initialDmmData.keys.toMutableSet()

    init {
        save()
    }

    private fun save() {
        sanitizeMap()
        fillWithReusedKeys()
        loopThroughRemainingTiles()
        saveToFile()
    }

    // Sanitize custom vars from values defined in the code
    private fun sanitizeMap() {
        for (x in 1..dmm.maxX) {
            for (y in 1..dmm.maxY) {
                val tile = dmm.getTile(x, y)

                for ((index, tileItem) in tile.tileItems.withIndex()) {
                    if (tileItem.customVars == null || tileItem.customVars.isEmpty()) {
                        continue // We won't find default vars for sure
                    }

                    val newVars = mutableMapOf<String, String>()

                    tileItem.customVars.forEach { (name, value) ->
                        if (value != tileItem.dmeItem.getVar(name)) {
                            newVars[name] = value
                        }
                    }

                    if (tileItem.customVars != newVars) {
                        tile.modifyItemVars(TileItemIdx(index), if (newVars.isEmpty()) null else newVars)
                    }
                }
            }
        }
    }

    private fun fillWithReusedKeys() {
        for (x in 1..outputDmmData.maxX) {
            for (y in 1..outputDmmData.maxY) {
                val newContent = dmm.getTileContentByLocation(x, y)
                val originalKey = initialDmmData.getKeyByLocation(x, y)
                val originalContent = initialDmmData.getTileContentByKey(originalKey)

                if (!outputDmmData.hasKeyByTileContent(newContent) && originalKey != null && originalContent == newContent) {
                    outputDmmData.addKeyAndTileContent(originalKey, newContent)
                    unusedKeys.remove(originalKey)
                }

                outputDmmData.addTileContentByLocation(x, y, newContent)
            }
        }
    }

    // This cycle will go through all tiles without keys.
    // Every time when we catch RecreateKeysException we clear all filled keys and generate new with new size.
    private fun loopThroughRemainingTiles() {
        while (true) {
            try {
                fillRemainingTiles()
            } catch (e: RecreateKeysException) {
                outputDmmData.keyLength = e.newSize
                clearKeyAndTileContent()
                continue
            }

            break
        }
    }

    // Fill remaining tiles (use unused keys or generate a new one)
    private fun fillRemainingTiles() {
        if (outputDmmData.hasLocationsWithoutContent()) {
            unusedKeys.clear()
            return // All locations have its own key
        }

        val locsWithoutKey = TLongArrayList()

        // Store two ints in one long. Yes, it matters. Yes, for performance reasons. No, don't repeat that at home.
        fun setValue(x: Int, y: Int): Long = (x.toLong() shl 32) or (y.toLong() and 0xffffffffL)

        fun getX(value: Long): Int = (value shr 32).toInt()
        fun getY(value: Long): Int = value.toInt()

        // Collect all locs without keys
        for (y in outputDmmData.maxY downTo 1) {
            for (x in 1..outputDmmData.maxX) {
                val tileContent = dmm.getTileContentByLocation(x, y)

                if (!outputDmmData.hasKeyByTileContent(tileContent)) {
                    locsWithoutKey.add(setValue(x, y))
                }
            }
        }

        // Try to find the most appropriate key for location
        for (unusedKey in unusedKeys.toSet()) {
            for (loc in locsWithoutKey) {
                val x = getX(loc)
                val y = getY(loc)
                if (initialDmmData.getKeyByLocation(x, y) == unusedKey) {
                    unusedKeys.remove(unusedKey)
                    outputDmmData.addKeyAndTileContent(unusedKey, dmm.getTileContentByLocation(x, y))
                    locsWithoutKey.remove(loc)
                    break
                }
            }
        }

        if (!locsWithoutKey.isEmpty) {
            keyGenerator.initKeysPool()
        }

        // Handle remaining locations
        for (loc in locsWithoutKey) {
            val x = getX(loc)
            val y = getY(loc)
            val tileContent = dmm.getTileContentByLocation(x, y)

            if (outputDmmData.hasKeyByTileContent(tileContent)) {
                continue
            }

            var key: String?

            if (unusedKeys.isEmpty()) {
                key = keyGenerator.createKey()
            } else {
                val it = unusedKeys.iterator()
                key = it.next()
                it.remove()
            }

            outputDmmData.addKeyAndTileContent(key, tileContent)
        }

        // Drop all unused keys.
        unusedKeys.clear()
    }

    private fun clearKeyAndTileContent() {
        outputDmmData.keys.toSet().forEach { outputDmmData.removeKeyAndTileContent(it) }
    }

    private fun saveToFile() {
        if (outputDmmData.isTgm) {
            outputDmmData.saveAsTGM(File(dmm.absMapPath.value))
        } else {
            outputDmmData.saveAsByond(File(dmm.absMapPath.value))
        }
    }
}
