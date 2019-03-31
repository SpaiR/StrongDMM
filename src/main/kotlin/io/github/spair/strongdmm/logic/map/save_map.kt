package io.github.spair.strongdmm.logic.map

import io.github.spair.dmm.io.DmmData
import io.github.spair.dmm.io.TileLocation
import java.io.File

fun saveMap(dmm: Dmm) {
    val outputDmmData = createDmmData(dmm.initialDmmData)
    val unusedKeys = dmm.initialDmmData.keys.toMutableSet()
    val keyGenerator = KeyGenerator(outputDmmData)

    // sanitize custom vars from values equals to defined in code
    for (x in 1..dmm.maxX) {
        for (y in 1..dmm.maxY) {
            dmm.getTile(x, y)!!.tileItems.forEach { tileItem ->
                tileItem.customVars.keys.toSet().forEach { name ->
                    if (tileItem.customVars[name] == tileItem.dmeItem.getVar(name)) {
                        tileItem.customVars.remove(name)
                    }
                }
            }
        }
    }

    // fill with reused keys
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

    // fill remaining tiles (use unused keys or generate new one)
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

    if (outputDmmData.isTgm) {
        outputDmmData.saveAsTGM(File(dmm.mapPath))
    } else {
        outputDmmData.saveAsByond(File(dmm.mapPath))
    }
}

private fun createDmmData(initialDmmData: DmmData) = DmmData().apply {
    maxX = initialDmmData.maxX
    maxY = initialDmmData.maxY
    keyLength = initialDmmData.keyLength
    isTgm = initialDmmData.isTgm
}

private class KeyGenerator(private val dmmData: DmmData) {

    private val limit = 65530
    private val base = 52

    private val base52keys = charArrayOf(
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    )

    fun createKey(): String {
        var freeKeys = Math.min(limit.toDouble(), Math.pow(base.toDouble(), dmmData.keyLength.toDouble())) - dmmData.keys.size
        var num = 1

        while (freeKeys > 0) {
            if (!dmmData.hasTileContentByKey(numToKey(num)))
                break
            num++
            freeKeys--
        }

        if (num > limit) {
            throw IllegalStateException("Maximum available num values is 65530. Current num: $num")
        }

        return numToKey(num)
    }

    private fun numToKey(num: Int): String {
        val result = StringBuilder()
        var currentNum = num

        while (currentNum > 0) {
            result.insert(0, base52keys[currentNum % base])
            currentNum = Math.floor(currentNum.toDouble() / base).toInt()
        }

        val keyLength = dmmData.keyLength

        if (result.length > keyLength) {
            throw IllegalStateException("Key length ($keyLength) cannot be less than result length (${result.length}")
        }

        val lengthDiff = keyLength - result.length
        if (lengthDiff != 0) {
            for (i in 0 until lengthDiff) {
                result.insert(0, base52keys[0])
            }
        }

        return result.toString()
    }
}
