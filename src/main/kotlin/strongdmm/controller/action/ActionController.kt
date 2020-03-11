package strongdmm.controller.action

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.byond.dmm.Dmm
import strongdmm.controller.action.undoable.Undoable
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventFrameController
import strongdmm.event.type.EventGlobal
import strongdmm.util.extension.getOrPut
import java.util.*

class ActionController : EventConsumer, EventSender {
    private val actionStacks: MutableMap<Dmm, ActionStack> = mutableMapOf()
    private val actionBalanceStorage: TObjectIntHashMap<Dmm> = TObjectIntHashMap()

    init {
        consumeEvent(Event.ActionController.AddAction::class.java, ::handleAddAction)
        consumeEvent(Event.ActionController.UndoAction::class.java, ::handleUndoAction)
        consumeEvent(Event.ActionController.RedoAction::class.java, ::handleRedoAction)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.OpenedMapChanged::class.java, ::handleOpenedMapChanged)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
    }

    private fun getMapActionStack(map: Dmm): ActionStack = actionStacks.getOrPut(map) { ActionStack() }

    private fun updateActionBalance(isPositive: Boolean) {
        sendEvent(Event.MapHolderController.FetchSelected { currentMap ->
            val currentBalance = actionBalanceStorage.getOrPut(currentMap) { 0 }
            val newBalance = if (isPositive) currentBalance + 1 else currentBalance - 1
            actionBalanceStorage.put(currentMap, newBalance)
            notifyActionBalanceChanged(currentMap)
        })
    }

    private fun notifyActionBalanceChanged(map: Dmm) {
        getMapActionStack(map).let { (undo, redo) ->
            sendEvent(EventGlobal.ActionStatusChanged(ActionStatus(undo.isNotEmpty(), redo.isNotEmpty())))
        }
    }

    private fun handleAddAction(event: Event<Undoable, Unit>) {
        sendEvent(Event.MapHolderController.FetchSelected { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                redo.clear()
                undo.push(event.body)
                updateActionBalance(true)
            }
        })
    }

    private fun handleUndoAction() {
        sendEvent(Event.MapHolderController.FetchSelected { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                redo.push(undo.pop().doAction())
                updateActionBalance(false)
                sendEvent(EventFrameController.Refresh())
            }
        })
    }

    private fun handleRedoAction() {
        sendEvent(Event.MapHolderController.FetchSelected { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                undo.push(redo.pop().doAction())
                updateActionBalance(true)
                sendEvent(EventFrameController.Refresh())
            }
        })
    }

    private fun handleEnvironmentReset() {
        actionStacks.clear()
        actionBalanceStorage.clear()
    }

    private fun handleOpenedMapChanged(event: Event<Dmm, Unit>) {
        notifyActionBalanceChanged(event.body)
    }

    private fun handleOpenedMapClosed(event: Event<Dmm, Unit>) {
        actionStacks.remove(event.body)
        actionBalanceStorage.remove(event.body)
        sendEvent(EventGlobal.ActionStatusChanged(ActionStatus(hasUndoAction = false, hasRedoAction = false)))
    }

    private data class ActionStack(
        val undo: Stack<Undoable> = Stack(),
        val redo: Stack<Undoable> = Stack()
    )
}
