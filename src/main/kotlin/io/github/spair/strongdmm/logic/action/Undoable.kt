package io.github.spair.strongdmm.logic.action

interface Undoable {
    fun doAction(): Undoable
}
