package strongdmm.controller.action.undoable

interface Undoable {
    fun doAction(): Undoable
}
