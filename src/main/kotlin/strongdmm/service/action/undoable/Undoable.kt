package strongdmm.service.action.undoable

interface Undoable {
    fun doAction(): Undoable
}
