package strongdmm.byond.dmm.save

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.parser.DmmData
import strongdmm.byond.dmm.parser.saveAsByond
import strongdmm.byond.dmm.parser.saveAsTGM
import strongdmm.controller.preferences.MapSaveMode
import strongdmm.controller.preferences.Preferences
import java.io.File

class SaveMap(
    private val dmm: Dmm,
    private val initialDmmData: DmmData,
    private val prefs: Preferences
) {
    private val outputDmmData: DmmData = DmmData().apply {
        isTgm = when (prefs.mapSaveMode) {
            MapSaveMode.PROVIDED -> initialDmmData.isTgm
            MapSaveMode.TGM -> true
            else -> false
        }

        keyLength = initialDmmData.keyLength
        setDmmSize(initialDmmData.maxZ, initialDmmData.maxY, initialDmmData.maxX)
    }

    private val keyGenerator: KeyGenerator = KeyGenerator(outputDmmData)
    private val unusedKeys: MutableSet<String> = initialDmmData.keys.toMutableSet()

    init {
        save()
    }

    private fun save() {
        if (prefs.sanitizeInitialVariables.get()) {
            sanitizeMap()
        }

        fillWithReusedKeys()
        loopThroughRemainingTiles()

        if (prefs.cleanUnusedKeys.get()) {
            removeUnusedKeys()
        }

        saveToFile()
    }

    // Sanitize custom vars from values defined in the code
    private fun sanitizeMap() {
        for (z in 1..dmm.maxZ) {
            for (y in 1..dmm.maxY) {
                for (x in 1..dmm.maxX) {
                    val tile = dmm.getTile(x, y, z)

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
                            tile.modifyItemVars(index, if (newVars.isEmpty()) null else newVars)
                        }
                    }
                }
            }
        }
    }

    private fun fillWithReusedKeys() {
        for (z in 1..dmm.maxZ) {
            for (y in 1..dmm.maxY) {
                for (x in 1..dmm.maxX) {
                    val newContent = dmm.getTileContentByLocation(x, y, z)
                    val originalKey = initialDmmData.getKeyByLocation(x, y, z)
                    val originalContent = initialDmmData.getTileContentByKey(originalKey)

                    if (!outputDmmData.hasKeyByTileContent(newContent) && originalKey != null && originalContent == newContent) {
                        outputDmmData.addKeyAndTileContent(originalKey, newContent)
                        unusedKeys.remove(originalKey)
                    }

                    outputDmmData.addTileContentByLocation(x, y, z, newContent)
                }
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

        data class Loc(val x: Int, val y: Int, val z: Int)

        val locsWithoutKey = mutableListOf<Loc>()

        // Collect all locs without keys
        for (z in 1..outputDmmData.maxZ) {
            for (y in outputDmmData.maxY downTo 1) {
                for (x in 1..outputDmmData.maxX) {
                    val tileContent = dmm.getTileContentByLocation(x, y, z)

                    if (!outputDmmData.hasKeyByTileContent(tileContent)) {
                        locsWithoutKey.add(Loc(x, y, z))
                    }
                }
            }
        }

        // Try to find the most appropriate key for location
        for (unusedKey in unusedKeys.toSet()) {
            for (loc in locsWithoutKey) {
                val (x, y, z) = loc

                if (initialDmmData.getKeyByLocation(x, y, z) == unusedKey) {
                    unusedKeys.remove(unusedKey)
                    outputDmmData.addKeyAndTileContent(unusedKey, dmm.getTileContentByLocation(x, y, z))
                    locsWithoutKey.remove(loc)
                    break
                }
            }
        }

        if (locsWithoutKey.isNotEmpty()) {
            keyGenerator.initKeysPool()
        }

        // Handle remaining locations
        for (loc in locsWithoutKey) {
            val (x, y, z) = loc
            val tileContent = dmm.getTileContentByLocation(x, y, z)

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

    private fun removeUnusedKeys() {
        if (outputDmmData.keysByTileContent.size != outputDmmData.tileContentsByKey.size) {
            val unusedKeys = outputDmmData.tileContentsByKey.keys - outputDmmData.keysByTileContent.values
            unusedKeys.forEach {
                outputDmmData.tileContentsByKey.remove(it)
            }
        }
    }

    private fun clearKeyAndTileContent() {
        outputDmmData.keys.toSet().forEach { outputDmmData.removeKeyAndTileContent(it) }
    }

    private fun saveToFile() {
        if (outputDmmData.isTgm) {
            outputDmmData.saveAsTGM(File(dmm.mapPath.absolute))
        } else {
            outputDmmData.saveAsByond(File(dmm.mapPath.absolute))
        }
    }
}
