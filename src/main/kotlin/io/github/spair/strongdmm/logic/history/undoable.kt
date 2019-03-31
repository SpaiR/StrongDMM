package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.menubar.MenuBarController
import java.util.Stack

interface Undoable {
    fun doAction(): Undoable
}

private val UNDO_STACK = Stack<Undoable>()
private val REDO_STACK = Stack<Undoable>()

private val ctrl by diInstance<MenuBarController>()

fun addUndoAction(undoable: Undoable) {
    UNDO_STACK.push(undoable)
    ctrl.switchUndo(true)
}

fun undoAction() {
    if (UNDO_STACK.isNotEmpty()) {
        REDO_STACK.push(UNDO_STACK.pop().doAction())

        ctrl.switchUndo(UNDO_STACK.isNotEmpty())
        ctrl.switchRedo(true)
    }
}

fun redoAction() {
    if (REDO_STACK.isNotEmpty()) {
        UNDO_STACK.push(REDO_STACK.pop().doAction())

        ctrl.switchRedo(REDO_STACK.isNotEmpty())
        ctrl.switchUndo(true)
    }
}
