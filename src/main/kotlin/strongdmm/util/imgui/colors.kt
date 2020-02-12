package strongdmm.util.imgui

fun col32(r: Int, g: Int, b: Int, a: Int): Int = (a shl 24) or (b shl 16) or (g shl 8) or (r shl 0)

val RED32: Int = col32(255, 0, 0, 255)
val GREEN32: Int = col32(0, 255, 0, 255)
