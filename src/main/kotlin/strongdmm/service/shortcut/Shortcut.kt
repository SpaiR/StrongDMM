package strongdmm.service.shortcut

import org.lwjgl.glfw.GLFW.*

data class Shortcut(
    val first: Int,
    val second: Int,
    val third: Int
) {
    companion object {
        val CONTROL_PAIR: Pair<Int, Int> = Pair(GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL)
        val ALT_PAIR: Pair<Int, Int> = Pair(GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT)
        val SHIFT_PAIR: Pair<Int, Int> = Pair(GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT)
        val PLUS_PAIR: Pair<Int, Int> = Pair(GLFW_KEY_KP_ADD, GLFW_KEY_EQUAL)
        val MINUS_PAIR: Pair<Int, Int> = Pair(GLFW_KEY_KP_SUBTRACT, GLFW_KEY_MINUS)
    }

    val weight: Int

    init {
        var weight = 0

        if (second != -1) {
            weight++
        }
        if (third != -1) {
            weight++
        }

        this.weight = weight
    }
}
