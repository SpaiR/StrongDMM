package io.github.spair.strongdmm.logic.map.save

import io.github.spair.dmm.io.DmmData
import kotlin.math.floor

class KeyGenerator(private val dmmData: DmmData) {

    companion object {
        private const val BASE: Double = 52.0

        // We can have only three tiers of keys, more here: https://secure.byond.com/forum/?post=2340796#comment23770802
        private const val TIER_1_LIMIT: Int = 51 // a-Z
        private const val TIER_2_LIMIT: Int = 2703 // aa-ZZ
        private const val TIER_3_LIMIT: Int = 65535 // aaa-ymp
    }

    private val base52keys: CharArray = charArrayOf(
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    )

    fun createKey(): String {
        val freeKeys = when {
            dmmData.keyLength == 1 -> TIER_1_LIMIT
            dmmData.keyLength == 2 -> TIER_2_LIMIT
            else -> TIER_3_LIMIT
        }

        var num = 0

        while (num <= freeKeys) {
            if (!dmmData.hasTileContentByKey(numToKey(num))) {
                break
            }
            num++
        }

        if (freeKeys == TIER_1_LIMIT && num > TIER_1_LIMIT) {
            throw RecreateKeysException(2)
        } else if (freeKeys == TIER_2_LIMIT && num > TIER_2_LIMIT) {
            throw RecreateKeysException(3)
        } else if (num > TIER_3_LIMIT) {
            throw IllegalStateException("Unable to create new key. Limit of keys is exceeded.")
        }

        return numToKey(num)
    }

    private fun numToKey(num: Int): String {
        val result = StringBuilder()
        var currentNum = num

        do {
            result.insert(0, base52keys[currentNum % BASE.toInt()])
            currentNum = floor(currentNum / BASE).toInt()
        } while (currentNum > 0)

        val lengthDiff = dmmData.keyLength - result.length

        if (lengthDiff != 0) {
            for (i in 0 until lengthDiff) {
                result.insert(0, base52keys[0])
            }
        }

        return result.toString()
    }
}
