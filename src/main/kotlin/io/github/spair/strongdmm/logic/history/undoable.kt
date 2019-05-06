package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import java.util.*

interface Undoable {
    fun doAction(): Undoable
}

private val UNDO_STACK = Stack<Undoable>()
private val REDO_STACK = Stack<Undoable>()

private val view by diInstance<MenuBarView>()

fun addUndoAction(undoable: Undoable) {
    REDO_STACK.clear()
    view.switchRedo(false)
    UNDO_STACK.push(undoable)
    view.switchUndo(true)
}

fun undoAction() {
    if (UNDO_STACK.isNotEmpty()) {
        REDO_STACK.push(UNDO_STACK.pop().doAction())

        view.switchUndo(UNDO_STACK.isNotEmpty())
        view.switchRedo(true)
    }
}

fun redoAction() {
    if (REDO_STACK.isNotEmpty()) {
        UNDO_STACK.push(REDO_STACK.pop().doAction())

        view.switchRedo(REDO_STACK.isNotEmpty())
        view.switchUndo(true)
    }
}
