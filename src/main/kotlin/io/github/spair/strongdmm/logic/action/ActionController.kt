package io.github.spair.strongdmm.logic.action

import io.github.spair.strongdmm.gui.TabbedMapPanelView
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.logic.EnvCleanable
import io.github.spair.strongdmm.logic.map.Dmm
import java.util.Stack

object ActionController : EnvCleanable {

    private val mapsStacks: MutableMap<Dmm?, Stacks> = hashMapOf()
    private val actionBalance: MutableMap<Int, Int> = hashMapOf()

    override fun clean() {
        with(getStacks()) {
            redoStack.clear()
            MenuBarView.switchRedo(false)
            undoStack.clear()
            MenuBarView.switchUndo(false)
        }
    }

    fun resetActionBalance(map: Dmm) {
        actionBalance[map.hashCode()] = 0
        TabbedMapPanelView.markMapModified(map, false)
    }

    fun hasChanges(map: Dmm): Boolean {
        val mapHash = map.hashCode()
        return if (actionBalance.containsKey(mapHash)) actionBalance[mapHash] != 0 else false
    }

    fun addUndoAction(undoable: Undoable, updateSelectedInstanceInfo: Boolean = true) {
        // Changes already happened
        if (updateSelectedInstanceInfo) {
            InstanceListView.updateSelectedInstanceInfo()
        }

        with(getStacks()) {
            redoStack.clear()
            undoStack.push(undoable)

            MenuBarView.switchRedo(false)
            MenuBarView.switchUndo(true)

            increaseActionBalance()
        }
    }

    fun undoAction() {
        with(getStacks()) {
            if (undoStack.isNotEmpty()) {
                redoStack.push(undoStack.pop().doAction())

                InstanceListView.updateSelectedInstanceInfo()
                SelectOperation.depickArea()
                MenuBarView.switchUndo(undoStack.isNotEmpty())
                MenuBarView.switchRedo(true)

                decreaseActionBalance()
            }
        }
    }

    fun redoAction() {
        with(getStacks()) {
            if (redoStack.isNotEmpty()) {
                undoStack.push(redoStack.pop().doAction())

                InstanceListView.updateSelectedInstanceInfo()
                SelectOperation.depickArea()
                MenuBarView.switchRedo(redoStack.isNotEmpty())
                MenuBarView.switchUndo(true)

                increaseActionBalance()
            }
        }
    }

    fun hasUndoActions(): Boolean = getStacks().undoStack.isNotEmpty()
    fun hasRedoActions(): Boolean = getStacks().redoStack.isNotEmpty()

    private fun getSelectedMapHash(): Int = MapView.getSelectedDmm()!!.hashCode()

    private fun increaseActionBalance() {
        val hash = getSelectedMapHash()
        val current = actionBalance.getOrDefault(hash, 0)
        val newValue = current + 1

        actionBalance[hash] = newValue
        handleBalance(newValue)
    }

    private fun decreaseActionBalance() {
        val hash = getSelectedMapHash()
        val current = actionBalance.getOrDefault(hash, 0)
        val newValue = current - 1

        actionBalance[hash] = newValue
        handleBalance(newValue)
    }

    private fun handleBalance(value: Int) {
        TabbedMapPanelView.markMapModified(MapView.getSelectedDmm()!!, value != 0)
    }

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
