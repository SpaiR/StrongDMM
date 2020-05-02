package strongdmm.service.action

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.PostInitialize
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerActionService
import strongdmm.event.type.service.TriggerFrameService
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.service.action.undoable.Undoable
import strongdmm.util.extension.getOrPut
import java.util.*

class ActionService : EventHandler, PostInitialize {
    private val actionStacks: MutableMap<Dmm, ActionStack> = mutableMapOf()
    private val actionBalanceStorage: TObjectIntHashMap<Dmm> = TObjectIntHashMap()

    init {
        consumeEvent(TriggerActionService.AddAction::class.java, ::handleAddAction)
        consumeEvent(TriggerActionService.UndoAction::class.java, ::handleUndoAction)
        consumeEvent(TriggerActionService.RedoAction::class.java, ::handleRedoAction)
        consumeEvent(TriggerActionService.ResetActionBalance::class.java, ::handleResetActionBalance)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
    }

    override fun postInit() {
        sendEvent(Provider.ActionControllerActionBalanceStorage(actionBalanceStorage))
    }

    private fun getMapActionStack(map: Dmm): ActionStack = actionStacks.getOrPut(map) { ActionStack() }

    private fun updateActionBalance(isPositive: Boolean) {
        sendEvent(TriggerMapHolderService.FetchSelectedMap { currentMap ->
            val currentBalance = actionBalanceStorage.getOrPut(currentMap) { 0 }
            val newBalance = if (isPositive) currentBalance + 1 else currentBalance - 1
            actionBalanceStorage.put(currentMap, newBalance)
            notifyActionBalanceChanged(currentMap)
        })
    }

    private fun notifyActionBalanceChanged(map: Dmm) {
        getMapActionStack(map).let { (undo, redo) ->
            sendEvent(Reaction.ActionStatusChanged(ActionStatus(undo.isNotEmpty(), redo.isNotEmpty())))
        }
    }

    private fun handleAddAction(event: Event<Undoable, Unit>) {
        sendEvent(TriggerMapHolderService.FetchSelectedMap { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                redo.clear()
                undo.push(event.body)
                updateActionBalance(true)
            }
        })
    }

    private fun handleUndoAction() {
        sendEvent(TriggerMapHolderService.FetchSelectedMap { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                if (undo.isEmpty()) {
                    return@FetchSelectedMap
                }

                redo.push(undo.pop().doAction())
                updateActionBalance(false)
                sendEvent(TriggerFrameService.RefreshFrame())
            }
        })
    }

    private fun handleRedoAction() {
        sendEvent(TriggerMapHolderService.FetchSelectedMap { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                if (redo.isEmpty()) {
                    return@FetchSelectedMap
                }

                undo.push(redo.pop().doAction())
                updateActionBalance(true)
                sendEvent(TriggerFrameService.RefreshFrame())
            }
        })
    }

    private fun handleResetActionBalance(event: Event<Dmm, Unit>) {
        actionBalanceStorage.put(event.body, 0)
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
        sendEvent(Reaction.ActionStatusChanged(ActionStatus(hasUndoAction = false, hasRedoAction = false)))
    }

    private data class ActionStack(
        val undo: Stack<Undoable> = Stack(),
        val redo: Stack<Undoable> = Stack()
    )
}
