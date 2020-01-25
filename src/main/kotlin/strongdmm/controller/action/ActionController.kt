package strongdmm.controller.action

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.extension.getOrPut
import java.util.*

class ActionController : EventConsumer, EventSender {
    private val actionStacks: MutableMap<Dmm, ActionStack> = mutableMapOf()
    private val actionBalanceStorage: TObjectIntHashMap<Dmm> = TObjectIntHashMap()

    init {
        consumeEvent(Event.ActionController.AddAction::class.java, ::handleAddAction)
        consumeEvent(Event.ActionController.UndoAction::class.java, ::handleUndoAction)
        consumeEvent(Event.ActionController.RedoAction::class.java, ::handleRedoAction)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
    }

    private fun getMapActionStack(map: Dmm): ActionStack = actionStacks.getOrPut(map) { ActionStack() }

    private fun updateActionBalance(isPositive: Boolean) {
        sendEvent(Event.MapController.FetchSelected { currentMap ->
            if (currentMap != null) {
                val currentBalance = actionBalanceStorage.getOrPut(currentMap) { 0 }
                val newBalance = if (isPositive) currentBalance + 1 else currentBalance - 1
                actionBalanceStorage.put(currentMap, newBalance)
                notifyActionBalanceChanged(currentMap)
            }
        })
    }

    private fun notifyActionBalanceChanged(map: Dmm) {
        getMapActionStack(map).let { (undo, redo) ->
            sendEvent(Event.Global.ActionStatusChanged(ActionStatus(undo.isNotEmpty(), redo.isNotEmpty())))
        }
    }

    private fun handleAddAction(event: Event<Undoable, Unit>) {
        sendEvent(Event.MapController.FetchSelected { currentMap ->
            if (currentMap != null) {
                getMapActionStack(currentMap).let { (undo, redo) ->
                    redo.clear()
                    undo.push(event.body)
                    updateActionBalance(true)
                }
            }
        })
    }

    private fun handleUndoAction() {
        sendEvent(Event.MapController.FetchSelected { currentMap ->
            if (currentMap != null) {
                getMapActionStack(currentMap).let { (undo, redo) ->
                    redo.push(undo.pop().doAction())
                    updateActionBalance(false)
                    sendEvent(Event.Global.RefreshFrame())
                }
            }
        })
    }

    private fun handleRedoAction() {
        sendEvent(Event.MapController.FetchSelected { currentMap ->
            if (currentMap != null) {
                getMapActionStack(currentMap).let { (undo, redo) ->
                    undo.push(redo.pop().doAction())
                    updateActionBalance(true)
                    sendEvent(Event.Global.RefreshFrame())
                }
            }
        })
    }

    private fun handleResetEnvironment() {
        actionStacks.clear()
        actionBalanceStorage.clear()
    }

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        notifyActionBalanceChanged(event.body)
    }

    private fun handleCloseMap(event: Event<Dmm, Unit>) {
        actionStacks.remove(event.body)
        actionBalanceStorage.remove(event.body)
    }

    private data class ActionStack(
        val undo: Stack<Undoable> = Stack(),
        val redo: Stack<Undoable> = Stack()
    )
}
