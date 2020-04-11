package strongdmm.controller.action.undoable

class MultiAction(
    private val actions: List<Undoable>
) : Undoable {
    override fun doAction(): Undoable {
        val reverseActions = mutableListOf<Undoable>()
        actions.forEach { reverseActions.add(it.doAction()) }
        return MultiAction(reverseActions)
    }
}
