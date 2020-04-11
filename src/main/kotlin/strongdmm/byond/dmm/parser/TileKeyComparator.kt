package strongdmm.byond.dmm.parser

class TileKeyComparator : Comparator<String> {
    override fun compare(key1: String?, key2: String?): Int {
        if (key1 == key2 || key1 == null || key2 == null) {
            return 0
        }

        for (i in 0 until key1.length.coerceAtMost(key2.length)) {
            val c1: Char = key1[i]
            val c2: Char = key2[i]

            if (Character.isLowerCase(c1) && Character.isUpperCase(c2)) {
                return -1
            } else if (Character.isUpperCase(c1) && Character.isLowerCase(c2)) {
                return 1
            } else {
                val charComparison = c1.compareTo(c2)
                if (charComparison != 0) {
                    return charComparison
                }
            }
        }

        return 0
    }
}
