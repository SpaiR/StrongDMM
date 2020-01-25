package strongdmm.controller.frame

import strongdmm.byond.dmi.IconSprite
import strongdmm.byond.dmm.Color

data class FrameMesh(
    val sprite: IconSprite,
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
    val color: Color,
    val depth: Float
)
