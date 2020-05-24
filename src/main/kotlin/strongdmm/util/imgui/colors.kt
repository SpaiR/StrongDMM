package strongdmm.util.imgui

import imgui.ImVec4

fun col32abgr(r: Int, g: Int, b: Int, a: Int): Int = (a shl 24) or (b shl 16) or (g shl 8) or (r shl 0)
fun col32argb(r: Int, g: Int, b: Int, a: Int): Int = (a shl 24) or (r shl 16) or (g shl 8) or (b shl 0)

val RED32: Int = col32abgr(255, 0, 0, 255)
val GREEN32: Int = col32abgr(0, 255, 0, 255)
val GREY32: Int = col32abgr(128, 128, 128, 255)

val LIGHT_BLUE_HIGHLIGHT32: Int = col32abgr(129, 178, 202, 255)

val GREEN_RGBA: ImVec4 = ImVec4(0f, 1f, 0f, 1f)
val RED_RGBA: ImVec4 = ImVec4(1f, 0f, 0f, 1f)
