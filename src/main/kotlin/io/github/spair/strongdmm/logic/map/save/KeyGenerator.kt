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

        val keysPool = mutableListOf<String>()

        // Put all possible keys into the pool ...
        for (num in 0..freeKeys) {
            val key = numToKey(num)
            if (!dmmData.hasTileContentByKey(key)) {
                keysPool.add(key)
            }
        }

        if (keysPool.isEmpty()) {
            when (freeKeys) {
                TIER_1_LIMIT -> throw RecreateKeysException(2)
                TIER_2_LIMIT -> throw RecreateKeysException(3)
                else -> throw IllegalStateException("Unable to create a new key. Limit of keys is exceeded.")
            }
        }

        // ... and pick randomly from it
        return keysPool[(0 until keysPool.size).random()]
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
