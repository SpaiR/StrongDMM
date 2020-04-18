package strongdmm.byond

const val NORTH = 1
const val SOUTH = 2
const val EAST = 4
const val WEST = 8

const val NORTHEAST = 5
const val NORTHWEST = 9
const val SOUTHEAST = 6
const val SOUTHWEST = 10

const val DEFAULT_DIR = SOUTH

fun relToDir(rel: Int): Int {
    return when (rel) {
        1 -> NORTH
        2 -> SOUTH
        3 -> EAST
        4 -> WEST
        5 -> NORTHEAST
        6 -> SOUTHEAST
        7 -> NORTHWEST
        8 -> SOUTHWEST
        else -> NORTH
    }
}

fun dirToRel(dir: Int): Int {
    return when (dir) {
        NORTH -> 1
        SOUTH -> 2
        EAST -> 3
        WEST -> 4
        NORTHEAST -> 5
        SOUTHEAST -> 6
        NORTHWEST -> 7
        SOUTHWEST -> 8
        else -> 1
    }
}

fun dirToStr(dir: Int): String {
    return when (dir) {
        NORTH -> "North"
        SOUTH -> "South"
        EAST -> "East"
        WEST -> "West"
        NORTHEAST -> "Northeast"
        SOUTHEAST -> "Southeast"
        NORTHWEST -> "Northwest"
        SOUTHWEST -> "Southwest"
        else -> ""
    }
}
