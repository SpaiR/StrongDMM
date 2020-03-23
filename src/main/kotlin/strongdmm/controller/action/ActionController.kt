package strongdmm.controller.action

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.byond.dmm.Dmm
import strongdmm.controller.action.undoable.Undoable
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventActionController
import strongdmm.event.type.controller.EventFrameController
import strongdmm.event.type.controller.EventMapHolderController
import strongdmm.util.extension.getOrPut
import java.util.*

class ActionController : EventConsumer, EventSender {
    private val actionStacks: MutableMap<Dmm, ActionStack> = mutableMapOf()
    private val actionBalanceStorage: TObjectIntHashMap<Dmm> = TObjectIntHashMap()

    init {
        consumeEvent(EventActionController.AddAction::class.java, ::handleAddAction)
        consumeEvent(EventActionController.UndoAction::class.java, ::handleUndoAction)
        consumeEvent(EventActionController.RedoAction::class.java, ::handleRedoAction)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
    }

    fun postInit() {
        sendEvent(EventGlobalProvider.ActionBalanceStorage(actionBalanceStorage))
    }

    private fun getMapActionStack(map: Dmm): ActionStack = actionStacks.getOrPut(map) { ActionStack() }

    private fun updateActionBalance(isPositive: Boolean) {
        sendEvent(EventMapHolderController.FetchSelectedMap { currentMap ->
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
        sendEvent(EventMapHolderController.FetchSelectedMap { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                redo.clear()
                undo.push(event.body)
                updateActionBalance(true)
            }
        })
    }

    private fun handleUndoAction() {
        sendEvent(EventMapHolderController.FetchSelectedMap { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                if (undo.isEmpty()) {
                    return@FetchSelectedMap
                }

                redo.push(undo.pop().doAction())
                updateActionBalance(false)
                sendEvent(EventFrameController.RefreshFrame())
            }
        })
    }

    private fun handleRedoAction() {
        sendEvent(EventMapHolderController.FetchSelectedMap { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                if (redo.isEmpty()) {
                    return@FetchSelectedMap
                }

                undo.push(redo.pop().doAction())
                updateActionBalance(true)
                sendEvent(EventFrameController.RefreshFrame())
            }
        })
    }

    private fun handleEnvironmentReset() {
        actionStacks.clear()
        actionBalanceStorage.clear()
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
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
