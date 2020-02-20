package strongdmm.util.imgui

import imgui.ImVec4

fun col32(r: Int, g: Int, b: Int, a: Int): Int = (a shl 24) or (b shl 16) or (g shl 8) or (r shl 0)

val RED32: Int = col32(255, 0, 0, 255)
val GREEN32: Int = col32(0, 255, 0, 255)

val GREEN_RGBA: ImVec4 = ImVec4(0f, 1f, 0f, 1f)
val RED_RGBA: ImVec4 = ImVec4(1f, 0f, 0f, 1f)
