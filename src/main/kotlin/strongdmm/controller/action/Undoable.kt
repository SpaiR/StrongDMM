package strongdmm.controller.action

interface Undoable {
    fun doAction(): Undoable
}
