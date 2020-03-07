package strongdmm.controller.shortcut

open class ShortcutHandler {
    val shortcuts: MutableMap<Shortcut, (() -> Unit)?> = mutableMapOf()

    fun addShortcut(first: Int, second: Int = -1, third: Int = -1, action: (() -> Unit)? = null) {
        shortcuts[Shortcut(first, second, third)] = action
    }

    fun addShortcut(firstPair: Pair<Int, Int>, second: Int = -1, third: Int = -1, action: (() -> Unit)? = null) {
        shortcuts[Shortcut(firstPair.first, second, third)] = action
        shortcuts[Shortcut(firstPair.second, second, third)] = action
    }

    fun addShortcut(firstPair: Pair<Int, Int>, secondPair: Pair<Int, Int>, third: Int = -1, action: (() -> Unit)? = null) {
        shortcuts[Shortcut(firstPair.first, secondPair.first, third)] = action
        shortcuts[Shortcut(firstPair.first, secondPair.second, third)] = action
        shortcuts[Shortcut(firstPair.second, secondPair.second, third)] = action
        shortcuts[Shortcut(firstPair.second, secondPair.first, third)] = action
    }

    fun handleShortcut(shortcut: Shortcut) {
        shortcuts[shortcut]?.invoke()
    }
}
