package io.github.spair.strongdmm.logic.history

interface Undoable {
    fun doAction(): Undoable
}
