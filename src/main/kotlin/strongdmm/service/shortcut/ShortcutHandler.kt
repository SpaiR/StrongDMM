package strongdmm.service.shortcut

import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.type.Reaction

@Suppress("LeakingThis")
open class ShortcutHandler : EventConsumer {
    companion object {
        val globalShortcuts: MutableSet<Shortcut> = mutableSetOf() // used to iterate through all registered shortcuts
    }

    private var isShortcutsBlocked: Boolean = false

    init {
        consumeEvent(Reaction.ShortcutTriggered::class.java, ::handleShortcutTriggered)
        consumeEvent(Reaction.ApplicationBlockChanged::class.java, ::handleApplicationBlockChanged)
    }

    private val shortcuts: MutableMap<Shortcut, (() -> Unit)?> = mutableMapOf()

    fun addShortcut(first: Int, second: Int = -1, third: Int = -1, action: (() -> Unit)? = null) {
        val shortcut = Shortcut(first, second, third)
        shortcuts[shortcut] = action
        globalShortcuts.add(shortcut)
    }

    fun addShortcut(firstPair: Pair<Int, Int>, second: Int = -1, third: Int = -1, action: (() -> Unit)? = null) {
        addShortcut(firstPair.first, second, third, action)
        addShortcut(firstPair.second, second, third, action)
    }

    fun addShortcut(firstPair: Pair<Int, Int>, secondPair: Pair<Int, Int>, third: Int = -1, action: (() -> Unit)? = null) {
        addShortcut(firstPair.first, secondPair.first, third, action)
        addShortcut(firstPair.first, secondPair.second, third, action)
        addShortcut(firstPair.second, secondPair.second, third, action)
        addShortcut(firstPair.second, secondPair.first, third, action)
    }

    private fun handleShortcutTriggered(event: Event<Shortcut, Unit>) {
        if (!isShortcutsBlocked) {
            shortcuts[event.body]?.invoke()
        }
    }

    private fun handleApplicationBlockChanged(event: Event<Boolean, Unit>) {
        isShortcutsBlocked = event.body
    }
}
