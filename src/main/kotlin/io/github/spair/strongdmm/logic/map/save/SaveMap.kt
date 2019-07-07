package io.github.spair.strongdmm.logic.map.save

import io.github.spair.dmm.io.DmmData
import io.github.spair.dmm.io.TileLocation
import io.github.spair.strongdmm.logic.Workspace
import io.github.spair.strongdmm.logic.map.Dmm
import java.io.File

class SaveMap(private val dmm: Dmm) {

    private val outputDmmData: DmmData
    private val keyGenerator: KeyGenerator
    private val unusedKeys: MutableSet<String>

    init {
        outputDmmData = DmmData().apply {
            dmm.initialDmmData.let { dmmData ->
                maxX = dmmData.maxX
                maxY = dmmData.maxY
                keyLength = dmmData.keyLength
                isTgm = Workspace.isTgmSaveMode()
            }
        }

        keyGenerator = KeyGenerator(outputDmmData)
        unusedKeys = dmm.initialDmmData.keys.toMutableSet()

        save()

        // Replace initial data with one which was saved, since it's in the file now.
        dmm.initialDmmData = outputDmmData
    }

    private fun save() {
        sanitizeMap()
        fillWithReusedKeys()
        loopThroughRemainingTiles()
        trimSizeIfNeeded()
        saveToFile()
    }

    // Sanitize custom vars from values defined in the code
    private fun sanitizeMap() {
        for (x in 1..dmm.maxX) {
            for (y in 1..dmm.maxY) {
                dmm.getTile(x, y)!!.let { tile ->
                    for (tileItem in tile.getTileItems()) {
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
                            tile.setTileItemVars(tileItem, (if (newVars.isEmpty()) null else newVars))
                        }
                    }
                }
            }
        }
    }

    private fun fillWithReusedKeys() {
        for (x in 1..outputDmmData.maxX) {
            for (y in 1..outputDmmData.maxY) {
                val location = TileLocation.of(x, y)
                val newContent = dmm.getTileContentByLocation(location)
                val originalKey = dmm.initialDmmData.getKeyByLocation(location)
                val originalContent = dmm.initialDmmData.getTileContentByKey(originalKey)

                if (!outputDmmData.hasKeyByTileContent(newContent) && originalKey != null && originalContent == newContent) {
                    outputDmmData.addKeyAndTileContent(originalKey, newContent)
                    unusedKeys.remove(originalKey)
                }

                outputDmmData.addTileContentByLocation(location, newContent)
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

    // Fill remaining tiles (use unused keys or generate new one)
    private fun fillRemainingTiles() {
        if (outputDmmData.tileContentsWithKeys.size != outputDmmData.tileContentsWithLocations.size) {
            for (y in outputDmmData.maxY downTo 1) {
                for (x in 1..outputDmmData.maxX) {
                    val location = TileLocation.of(x, y)
                    val tileContent = dmm.getTileContentByLocation(location)

                    if (!outputDmmData.hasKeyByTileContent(tileContent)) {
                        var key: String? = null

                        if (unusedKeys.isEmpty()) {
                            key = keyGenerator.createKey()
                        } else {
                            // from all unused keys we will try to find the most appropriate
                            for (unusedKey in unusedKeys) {
                                if (dmm.initialDmmData.getKeyByLocation(location) == unusedKey) {
                                    key = unusedKey
                                    unusedKeys.remove(key)
                                    break
                                }
                            }

                            // if no appropriate key found use first available
                            if (key == null) {
                                val it = unusedKeys.iterator()
                                key = it.next()
                                it.remove()
                            }
                        }

                        outputDmmData.addKeyAndTileContent(key, tileContent)
                    }
                }
            }
        }

        // Drop down all unused keys for sure.
        unusedKeys.clear()
    }

    // If our keys could be fitted in lesser key length, then we will create all keys with lesser tier from scratch.
    private fun trimSizeIfNeeded() {
        var newSize = -1
        val keysNumber = outputDmmData.keys.size - 1 // Value for tier limit starts from zero.

        if (keysNumber <= KeyGenerator.TIR_1_LIMIT && outputDmmData.keyLength >= 2) {
            newSize = 1
        } else if (keysNumber <= KeyGenerator.TIR_2_LIMIT && outputDmmData.keyLength >= 3) {
            newSize = 2
        }

        if (newSize != -1) {
            clearKeyAndTileContent()
            outputDmmData.keyLength = newSize
            fillRemainingTiles()
        }
    }

    private fun clearKeyAndTileContent() {
        outputDmmData.keys.toSet().forEach { outputDmmData.removeKeyAndTileContent(it) }
    }

    private fun saveToFile() {
        if (outputDmmData.isTgm) {
            outputDmmData.saveAsTGM(File(dmm.mapPath))
        } else {
            outputDmmData.saveAsByond(File(dmm.mapPath))
        }
    }
}
