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
    val color: Color = DEFAULT_COLOR,
    val type: String = "",
    val plane: Float = 0f,
    val layer: Float = 0f
)

data class Color(val red: Float = 1f, val green: Float = 1f, val blue: Float = 1f, val alpha: Float = 1f)
val DEFAULT_COLOR = Color()
