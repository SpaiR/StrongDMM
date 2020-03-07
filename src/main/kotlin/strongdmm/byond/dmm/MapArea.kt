package strongdmm.byond.dmm

data class MapArea(
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int
) {
    fun isInBounds(x: Int, y: Int): Boolean = x in x1..x2 && y in y1..y2
}
