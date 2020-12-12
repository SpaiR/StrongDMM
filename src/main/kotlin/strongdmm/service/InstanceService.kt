package strongdmm.service

import strongdmm.application.Service
import strongdmm.byond.*
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.*
import strongdmm.event.ui.TriggerConfirmationDialogUi
import strongdmm.event.ui.TriggerEditVarsDialogUi
import strongdmm.event.ui.TriggerObjectPanelUi
import strongdmm.service.action.undoable.TileItemAddAction
import strongdmm.service.action.undoable.TileItemRemoveAction
import strongdmm.service.dmi.DmiCache
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogData
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogStatus

class InstanceService : Service {
    private lateinit var providedDmiCache: DmiCache

    init {
        EventBus.sign(ProviderDmiService.DmiCache::class.java, ::handleProviderDmiCache)
        EventBus.sign(TriggerInstanceService.GenerateInstancesFromIconStates::class.java, ::handleGenerateInstancesFromIconStates)
        EventBus.sign(TriggerInstanceService.GenerateInstancesFromDirections::class.java, ::handleGenerateInstancesFromDirections)
        EventBus.sign(TriggerInstanceService.FindInstancePositionsByType::class.java, ::handleFindInstancePositionsByType)
        EventBus.sign(TriggerInstanceService.FindInstancePositionsById::class.java, ::handleFindInstancePositionsById)
        EventBus.sign(TriggerInstanceService.EditInstance::class.java, ::handleEditInstance)
        EventBus.sign(TriggerInstanceService.DeleteInstance::class.java, ::handleDeleteInstance)
    }

    private fun handleProviderDmiCache(event: Event<DmiCache, Unit>) {
        providedDmiCache = event.body
    }

    private fun handleGenerateInstancesFromIconStates(event: Event<TileItem, Unit>) {
        providedDmiCache.getDmi(event.body.icon)?.let { dmi ->
            EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment { dme ->
                val itemType = event.body.type
                val dmeItem = dme.getItem(itemType)!!
                val initialIconState = dmeItem.getVarText(VAR_ICON_STATE) ?: ""

                dmi.iconStates.keys.filter { it != initialIconState }.let { iconStates ->
                    if (iconStates.isNotEmpty()) {
                        iconStates.forEach { iconStateName ->
                            GlobalTileItemHolder.getOrCreate(itemType, mutableMapOf(VAR_ICON_STATE to "\"$iconStateName\""))
                        }

                        EventBus.post(TriggerObjectPanelUi.Update())
                    }
                }
            })
        }
    }

    private fun handleGenerateInstancesFromDirections(event: Event<TileItem, Unit>) {
        val tileItem = event.body
        providedDmiCache.getIconState(tileItem.icon, tileItem.iconState)?.let { iconState ->
            EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment { dme ->
                val dmeItem = dme.getItem(tileItem.type)!!
                val initialDir = dmeItem.getVarInt(VAR_DIR) ?: DEFAULT_DIR

                when (iconState.dirs) {
                    4 -> {
                        arrayOf(NORTH, SOUTH, EAST, WEST).filter { it != initialDir }.forEach { dir ->
                            GlobalTileItemHolder.getOrCreate(tileItem.type, mutableMapOf(VAR_DIR to dir.toString()))
                        }
                    }
                    8 -> {
                        arrayOf(NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST).filter { it != initialDir }.forEach { dir ->
                            GlobalTileItemHolder.getOrCreate(tileItem.type, mutableMapOf(VAR_DIR to dir.toString()))
                        }
                    }
                }

                EventBus.post(TriggerObjectPanelUi.Update())
            })
        }
    }

    private fun handleFindInstancePositionsByType(event: Event<Pair<MapArea, String>, List<Pair<TileItem, MapPos>>>) {
        val positions = mutableListOf<Pair<TileItem, MapPos>>()

        EventBus.post(TriggerMapHolderService.FetchSelectedMap { map ->
            if (event.body.second.isNotEmpty()) {
                val (x1, y1, x2, y2) = event.body.first

                for (z in (1..map.maxZ)) {
                    for (x in (x1..x2)) {
                        for (y in (y1..y2)) {
                            map.getTileItemsId(x, y, z).forEach { tileItemId ->
                                val tileItem = GlobalTileItemHolder.getById(tileItemId)
                                if (tileItem.type == event.body.second) {
                                    positions.add(Pair(tileItem, MapPos(x, y, z)))
                                }
                            }
                        }
                    }
                }
            }
        })

        event.reply(positions)
    }

    private fun handleFindInstancePositionsById(event: Event<Pair<MapArea, Long>, List<Pair<TileItem, MapPos>>>) {
        val positions = mutableListOf<Pair<TileItem, MapPos>>()

        EventBus.post(TriggerMapHolderService.FetchSelectedMap { map ->
            val (x1, y1, x2, y2) = event.body.first

            for (z in (1..map.maxZ)) {
                for (x in (x1..x2)) {
                    for (y in (y1..y2)) {
                        map.getTileItemsId(x, y, z).forEach { tileItemId ->
                            val tileItem = GlobalTileItemHolder.getById(tileItemId)
                            if (tileItem.id == event.body.second) {
                                positions.add(Pair(tileItem, MapPos(x, y, z)))
                            }
                        }
                    }
                }
            }
        })

        event.reply(positions)
    }

    private fun handleEditInstance(event: Event<TileItem, Unit>) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { map ->
            EventBus.post(TriggerActionService.BatchActions { batchCompleteCallback ->
                EventBus.post(TriggerEditVarsDialogUi.OpenWithTileItem(event.body) { newTileItem ->
                    EventBus.post(TriggerInstanceService.FindInstancePositionsById(Pair(map.getMapArea(), event.body.id)) { instancePositions ->
                        EventBus.post(TriggerMapModifierService.ReplaceTileItemsByIdWithIdInPositions(Pair(newTileItem.id, instancePositions)))

                        if (!event.body.isDefaultInstance()) {
                            GlobalTileItemHolder.remove(event.body)

                            EventBus.post(TriggerActionService.QueueUndoable(TileItemAddAction(event.body) {
                                EventBus.post(TriggerObjectPanelUi.Update())
                            }))
                        }

                        EventBus.post(TriggerActionService.QueueUndoable(TileItemRemoveAction(newTileItem) {
                            EventBus.post(TriggerObjectPanelUi.Update())
                        }))
                    })

                    batchCompleteCallback()
                })
            })
        })
    }

    private fun handleDeleteInstance(event: Event<TileItem, Unit>) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { map ->
            EventBus.post(TriggerActionService.BatchActions { batchCompleteCallback ->
                EventBus.post(TriggerInstanceService.FindInstancePositionsById(Pair(map.getMapArea(), event.body.id)) { instancePositions ->
                    val question = "Do you really want to delete an instance? (found in ${instancePositions.size} places)"
                    val confirmationDialogData = ConfirmationDialogData(question = question)

                    EventBus.post(TriggerConfirmationDialogUi.Open(confirmationDialogData) {
                        if (it == ConfirmationDialogStatus.YES) {
                            EventBus.post(TriggerMapModifierService.DeleteTileItemsWithIdInPositions(instancePositions))

                            if (!event.body.isDefaultInstance()) {
                                GlobalTileItemHolder.remove(event.body)

                                EventBus.post(TriggerActionService.QueueUndoable(TileItemAddAction(event.body) {
                                    EventBus.post(TriggerObjectPanelUi.Update())
                                }))
                            }
                        }

                        batchCompleteCallback()
                    })
                })
            })
        })
    }
}
