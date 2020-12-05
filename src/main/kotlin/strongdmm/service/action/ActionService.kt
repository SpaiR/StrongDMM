package strongdmm.service.action

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.application.PostInitialize
import strongdmm.application.Service
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.ProviderActionService
import strongdmm.event.type.service.TriggerActionService
import strongdmm.event.type.service.TriggerFrameService
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.service.action.undoable.MultiAction
import strongdmm.service.action.undoable.Undoable
import strongdmm.util.extension.getOrPut
import java.util.*

class ActionService : Service, PostInitialize {
    private var isBatchingActions: Boolean = false
    private val actionBatchList: MutableList<Undoable> = mutableListOf()

    private val actionStacks: MutableMap<Dmm, ActionStack> = mutableMapOf()
    private val actionBalanceStorage: TObjectIntHashMap<Dmm> = TObjectIntHashMap()

    init {
        EventBus.sign(TriggerActionService.BatchActions::class.java, ::handleBatchActions)
        EventBus.sign(TriggerActionService.QueueUndoable::class.java, ::handleQueueUndoable)
        EventBus.sign(TriggerActionService.UndoAction::class.java, ::handleUndoAction)
        EventBus.sign(TriggerActionService.RedoAction::class.java, ::handleRedoAction)
        EventBus.sign(TriggerActionService.ResetActionBalance::class.java, ::handleResetActionBalance)
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
    }

    override fun postInit() {
        EventBus.post(ProviderActionService.ActionBalanceStorage(ActionBalanceStorage(actionBalanceStorage)))
    }

    private fun getMapActionStack(map: Dmm): ActionStack = actionStacks.getOrPut(map) { ActionStack() }

    private fun updateActionBalance(isPositive: Boolean) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { currentMap ->
            val currentBalance = actionBalanceStorage.getOrPut(currentMap) { 0 }
            val newBalance = if (isPositive) currentBalance + 1 else currentBalance - 1
            actionBalanceStorage.put(currentMap, newBalance)
            notifyActionBalanceChanged(currentMap)
        })
    }

    private fun notifyActionBalanceChanged(map: Dmm) {
        getMapActionStack(map).let { (undo, redo) ->
            EventBus.post(Reaction.ActionStatusChanged(ActionStatus(undo.isNotEmpty(), redo.isNotEmpty())))
        }
    }

    private fun handleBatchActions(event: Event<(() -> Unit) -> Unit, Unit>) {
        isBatchingActions = true

        event.body {
            isBatchingActions = false

            if (actionBatchList.isNotEmpty()) {
                EventBus.post(TriggerActionService.QueueUndoable(MultiAction(actionBatchList.toList())))
                actionBatchList.clear()
            }
        }
    }

    private fun handleQueueUndoable(event: Event<Undoable, Unit>) {
        if (isBatchingActions) {
            actionBatchList.add(event.body)
            return
        }

        EventBus.post(TriggerMapHolderService.FetchSelectedMap { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                redo.clear()
                undo.push(event.body)
                updateActionBalance(true)
            }
        })
    }

    private fun handleUndoAction() {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                if (undo.isEmpty()) {
                    return@FetchSelectedMap
                }

                redo.push(undo.pop().doAction())
                updateActionBalance(false)
                EventBus.post(TriggerFrameService.RefreshFrame())
            }
        })
    }

    private fun handleRedoAction() {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { currentMap ->
            getMapActionStack(currentMap).let { (undo, redo) ->
                if (redo.isEmpty()) {
                    return@FetchSelectedMap
                }

                undo.push(redo.pop().doAction())
                updateActionBalance(true)
                EventBus.post(TriggerFrameService.RefreshFrame())
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
        EventBus.post(Reaction.ActionStatusChanged(ActionStatus(hasUndoAction = false, hasRedoAction = false)))
    }

    private data class ActionStack(
        val undo: Stack<Undoable> = Stack(),
        val redo: Stack<Undoable> = Stack()
    )
}
