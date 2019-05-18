package io.github.spair.strongdmm.logic.history

class MultipleAction(private val actions: List<Undoable>) : Undoable {
    override fun doAction(): Undoable {
        val reverseActions = mutableListOf<Undoable>()
        actions.forEach { action ->
            reverseActions.add(action.doAction())
        }
        return MultipleAction(reverseActions)
    }
}
