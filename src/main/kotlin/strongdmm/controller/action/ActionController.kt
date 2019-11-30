package strongdmm.controller.action

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.extension.getOrPut
import java.util.Stack

class ActionController : EventConsumer, EventSender {
    private val actionStacks: MutableMap<Dmm, ActionStack> = mutableMapOf()
    private val actionBalanceStorage: TObjectIntHashMap<Dmm> = TObjectIntHashMap()

    init {
        consumeEvent(Event.ActionController.AddAction::class.java, ::handleAddAction)
        consumeEvent(Event.ActionController.UndoAction::class.java, ::handleUndoAction)
        consumeEvent(Event.ActionController.RedoAction::class.java, ::handleRedoAction)
    }

    private fun handleAddAction(event: Event<Undoable, Unit>) {
        getCurrentActionStack()?.let { (undo, redo) ->
            redo.clear()
            undo.push(event.body)
            updateActionBalance(true)
        }
    }

    private fun handleUndoAction() {
        getCurrentActionStack()?.let { (undo, redo) ->
            redo.push(undo.pop().doAction())
            updateActionBalance(false)
            sendEvent(Event.Global.RefreshFrame())
        }
    }

    private fun handleRedoAction() {
        getCurrentActionStack()?.let { (undo, redo) ->
            undo.push(redo.pop().doAction())
            updateActionBalance(true)
            sendEvent(Event.Global.RefreshFrame())
        }
    }

    private fun getCurrentActionStack(): ActionStack? {
        var actionStack: ActionStack? = null

        sendEvent(Event.MapController.FetchSelected { currentMap ->
            if (currentMap != null) {
                actionStack = actionStacks.getOrPut(currentMap) { ActionStack() }
            }
        })

        return actionStack
    }

    private fun updateActionBalance(isPositive: Boolean) {
        sendEvent(Event.MapController.FetchSelected { currentMap ->
            if (currentMap != null) {
                val currentBalance = actionBalanceStorage.getOrPut(currentMap) { 0 }
                val newBalance = if (isPositive) currentBalance + 1 else currentBalance - 1
                actionBalanceStorage.put(currentMap, newBalance)

                actionStacks[currentMap]!!.let { (undo, redo) ->
                    sendEvent(Event.Global.ActionStatusChanged(ActionStatus(undo.isNotEmpty(), redo.isNotEmpty())))
                }
            }
        })
    }

    private data class ActionStack(
        val undo: Stack<Undoable> = Stack(),
        val redo: Stack<Undoable> = Stack()
    )
}
