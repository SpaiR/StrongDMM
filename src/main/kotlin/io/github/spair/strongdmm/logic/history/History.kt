package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.TabbedMapPanelView
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.logic.EnvCleanable
import io.github.spair.strongdmm.logic.map.Dmm
import java.util.Stack

object History : EnvCleanable {

    private val mapsStacks: MutableMap<Dmm?, Stacks> = hashMapOf()

    override fun clean() {
        with(getStacks()) {
            redoStack.clear()
            MenuBarView.switchRedo(false)
            undoStack.clear()
            MenuBarView.switchUndo(false)
        }
    }

    fun addUndoAction(undoable: Undoable) {
        with(getStacks()) {
            redoStack.clear()
            MenuBarView.switchRedo(false)
            undoStack.push(undoable)
            MenuBarView.switchUndo(true)

            TabbedMapPanelView.markMapModified(true)
        }
    }

    fun undoAction() {
        with(getStacks()) {
            if (undoStack.isNotEmpty()) {
                redoStack.push(undoStack.pop().doAction())
                SelectOperation.depickArea()
                MenuBarView.switchUndo(undoStack.isNotEmpty())
                MenuBarView.switchRedo(true)

                if (undoStack.isEmpty()) {
                    TabbedMapPanelView.markMapModified(false)
                }
            }
        }
    }

    fun redoAction() {
        with(getStacks()) {
            if (redoStack.isNotEmpty()) {
                undoStack.push(redoStack.pop().doAction())
                SelectOperation.depickArea()
                MenuBarView.switchRedo(redoStack.isNotEmpty())
                MenuBarView.switchUndo(true)

                TabbedMapPanelView.markMapModified(true)
            }
        }
    }

    fun hasUndoActions(): Boolean = getStacks().undoStack.isNotEmpty()
    fun hasRedoActions(): Boolean = getStacks().redoStack.isNotEmpty()

    fun clearUnusedActions(openedMaps: List<Dmm>) {
        mapsStacks.keys.filter { it !in openedMaps }.forEach { mapsStacks.remove(it) }
    }

    private fun getStacks(): Stacks {
        val selectedMap = MapView.getSelectedDmm()
        return mapsStacks.computeIfAbsent(selectedMap) { Stacks() }
    }

    private class Stacks {
        val undoStack: Stack<Undoable> = Stack()
        val redoStack: Stack<Undoable> = Stack()
    }
}
