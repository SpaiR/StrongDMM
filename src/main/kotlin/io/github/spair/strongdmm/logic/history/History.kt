package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.menubar.MenuBarView
import java.util.*

object History {

    private val UNDO_STACK = Stack<Undoable>()
    private val REDO_STACK = Stack<Undoable>()

    fun addUndoAction(undoable: Undoable) {
        REDO_STACK.clear()
        MenuBarView.switchRedo(false)
        UNDO_STACK.push(undoable)
        MenuBarView.switchUndo(true)
    }

    fun undoAction() {
        if (UNDO_STACK.isNotEmpty()) {
            REDO_STACK.push(UNDO_STACK.pop().doAction())

            MenuBarView.switchUndo(UNDO_STACK.isNotEmpty())
            MenuBarView.switchRedo(true)
        }
    }

    fun redoAction() {
        if (REDO_STACK.isNotEmpty()) {
            UNDO_STACK.push(REDO_STACK.pop().doAction())

            MenuBarView.switchRedo(REDO_STACK.isNotEmpty())
            MenuBarView.switchUndo(true)
        }
    }

    fun clearActions() {
        REDO_STACK.clear()
        MenuBarView.switchRedo(false)
        UNDO_STACK.clear()
        MenuBarView.switchUndo(false)
    }
}
