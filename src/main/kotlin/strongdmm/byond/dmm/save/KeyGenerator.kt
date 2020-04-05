package strongdmm.byond.dmm.save

import gnu.trove.list.array.TIntArrayList
import strongdmm.byond.dmm.parser.DmmData
import kotlin.math.floor

class KeyGenerator(private val dmmData: DmmData) {
    companion object {
        private const val BASE: Double = 52.0

        // We can have only three tiers of keys, more here: https://secure.byond.com/forum/?post=2340796#comment23770802
        private const val TIER_1_LIMIT: Int = 51 // a-Z
        private const val TIER_2_LIMIT: Int = 2703 // aa-ZZ
        private const val TIER_3_LIMIT: Int = 65528 // aaa-ymi

        // Calculated tiers with regard to 'a~' and 'a~~' keys
        private const val REAL_TIER_1_LIMIT: Int = TIER_1_LIMIT
        private const val REAL_TIER_2_LIMIT: Int = TIER_1_LIMIT + TIER_2_LIMIT + 1
        private const val REAL_TIER_3_LIMIT: Int = REAL_TIER_2_LIMIT + TIER_3_LIMIT + 1

        private val KEYS: Array<String> = Array(REAL_TIER_3_LIMIT + 1) { "" }

        init {
            val builder = StringBuilder()
            val base52keys = charArrayOf(
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
            )

            fun numToKey(num: Int, keyLength: Int): String {
                var currentNum = num

                do {
                    builder.insert(0, base52keys[currentNum % BASE.toInt()])
                    currentNum = floor(currentNum / BASE).toInt()
                } while (currentNum > 0)

                val lengthDiff = keyLength - builder.length

                if (lengthDiff != 0) {
                    for (i in 0 until lengthDiff) {
                        builder.insert(0, 'a')
                    }
                }

                val result = builder.toString()
                builder.clear()
                return result
            }

            var index = 0

            // a-Z
            (0..TIER_1_LIMIT).forEach {
                KEYS[index++] = numToKey(it, 1)
            }

            // aa-aZ
            (0..TIER_1_LIMIT).forEach {
                KEYS[index++] = numToKey(it, 2)
            }

            // ba-ZZ
            ((TIER_1_LIMIT + 1)..TIER_2_LIMIT).forEach {
                KEYS[index++] = numToKey(it, 2)
            }

            // aaa-aZZ
            (0..TIER_2_LIMIT).forEach {
                KEYS[index++] = numToKey(it, 3)
            }

            // baa-ymi
            ((TIER_2_LIMIT + 1)..TIER_3_LIMIT).forEach {
                KEYS[index++] = numToKey(it, 3)
            }
        }
    }

    private lateinit var keysPool: TIntArrayList
    private var freeKeys: Int = 0

    fun initKeysPool() {
        keysPool = TIntArrayList()

        freeKeys = when (dmmData.keyLength) {
            1 -> REAL_TIER_1_LIMIT
            2 -> REAL_TIER_2_LIMIT
            else -> REAL_TIER_3_LIMIT
        }

        val border = when (freeKeys) {
            REAL_TIER_2_LIMIT -> REAL_TIER_1_LIMIT + 1
            REAL_TIER_3_LIMIT -> REAL_TIER_2_LIMIT + 1
            else -> 0
        }

        // Put all possible keys into the pool ...
        for (num in border..freeKeys) {
            val key = KEYS[num]
            if (!dmmData.hasTileContentByKey(key)) {
                keysPool.add(num)
            }
        }
    }

    fun createKey(): String {
        if (keysPool.isEmpty) {
            when (freeKeys) {
                REAL_TIER_1_LIMIT -> throw RecreateKeysException(2)
                REAL_TIER_2_LIMIT -> throw RecreateKeysException(3)
                else -> throw IllegalStateException("Unable to create a new key. Limit of keys exceeded.")
            }
        }

        // ... and pick randomly from it
        val index = (0 until keysPool.size()).random()
        val key = keysPool[index]
        keysPool.removeAt(index)
        return KEYS[key]
    }
}
