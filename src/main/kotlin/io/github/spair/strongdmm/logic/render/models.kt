package io.github.spair.strongdmm.logic.render

data class RenderInstance(
    val locX: Float,
    val locY: Float,
    val textureId: Int,
    val u1: Float = 0f,
    val v1: Float = 0f,
    val u2: Float = 1f,
    val v2: Float = 1f,
    val width: Float = 32f,
    val height: Float = 32f,
    val color: Color = Color(1f, 1f, 1f, 1f)
)

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float)
