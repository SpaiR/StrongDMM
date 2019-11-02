package strongdmm.controller.frame

import strongdmm.byond.dmi.IconSprite

data class FrameMesh(
    val sprite: IconSprite,
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
    val color: Color,
    val depth: Float
)
