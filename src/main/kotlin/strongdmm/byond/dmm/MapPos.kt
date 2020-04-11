package strongdmm.byond.dmm

import strongdmm.util.OUT_OF_BOUNDS

data class MapPos(
    val x: Int,
    val y: Int,
    val z: Int = -1
) {
    fun isOutOfBounds(): Boolean = x == OUT_OF_BOUNDS || y == OUT_OF_BOUNDS
    fun isNotOutOfBounds(): Boolean = x != OUT_OF_BOUNDS && y != OUT_OF_BOUNDS
}
