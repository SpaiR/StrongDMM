package io.github.spair.strongdmm.logic.map

data class CoordArea(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
    fun shiftToPoint(x: Int, y: Int) = CoordArea(x, y, x + x2 - x1, y + y2 - y1)
}
