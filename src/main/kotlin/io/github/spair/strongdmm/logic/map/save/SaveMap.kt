package io.github.spair.strongdmm.logic.map.save

import io.github.spair.dmm.io.DmmData
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
                setDmmSize(dmmData.maxX, dmmData.maxY)
                keyLength = dmmData.keyLength
                isTgm = Workspace.isTgmSaveMode()
            }
        }

        keyGenerator = KeyGenerator(outputDmmData)
        unusedKeys = dmm.initialDmmData.keys.toMutableSet()

        save()
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
        for (x in 1..dmm.getMaxX()) {
            for (y in 1..dmm.getMaxY()) {
                val tile = dmm.getTile(x, y)!!

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

    private fun fillWithReusedKeys() {
        for (x in 1..outputDmmData.maxX) {
            for (y in 1..outputDmmData.maxY) {
                val newContent = dmm.getTileContentByLocation(x, y)
                val originalKey = dmm.initialDmmData.getKeyByLocation(x, y)
                val originalContent = dmm.initialDmmData.getTileContentByKey(originalKey)

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

        val locsWithoutKey = mutableListOf<Long>()

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
                if (dmm.initialDmmData.getKeyByLocation(x, y) == unusedKey) {
                    unusedKeys.remove(unusedKey)
                    outputDmmData.addKeyAndTileContent(unusedKey, dmm.getTileContentByLocation(x, y))
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

        // Drop down all unused keys for sure.
        unusedKeys.clear()
    }

    // Even if we can fit our map keys in a less length the editor won't do that.
    // It's okay to recreate all keys from scratch when we overflow our limit.
    // But when our limit was increased we can easily use it to represent the map even if it has two unique keys.
    // At the same time, key length reduction will result in an abnormal map diff, which may result in a lot of conflicts.
    // Thus the only case when the Editor will reduce key length is when we have only one key on the map. Just because I can.
    private fun trimSizeIfNeeded() {
        if (outputDmmData.keys.size == 1) {
            clearKeyAndTileContent()
            outputDmmData.keyLength = 1
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
