package strongdmm.service

import strongdmm.Service
import strongdmm.byond.*
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.service.*
import strongdmm.event.type.ui.TriggerConfirmationDialogUi
import strongdmm.event.type.ui.TriggerObjectPanelUi
import strongdmm.service.action.undoable.TileItemAddAction
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogData
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogStatus

class InstanceService : Service, EventHandler {
    init {
        consumeEvent(TriggerInstanceService.GenerateInstancesFromIconStates::class.java, ::handleGenerateInstancesFromIconStates)
        consumeEvent(TriggerInstanceService.GenerateInstancesFromDirections::class.java, ::handleGenerateInstancesFromDirections)
        consumeEvent(TriggerInstanceService.FindInstancePositionsByType::class.java, ::handleFindInstancePositionsByType)
        consumeEvent(TriggerInstanceService.FindInstancePositionsById::class.java, ::handleFindInstancePositionsById)
        consumeEvent(TriggerInstanceService.DeleteInstance::class.java, ::handleDeleteInstance)
    }

    private fun handleGenerateInstancesFromIconStates(event: Event<TileItem, Unit>) {
        GlobalDmiHolder.getDmi(event.body.icon)?.let { dmi ->
            sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment { dme ->
                val itemType = event.body.type
                val dmeItem = dme.getItem(itemType)!!
                val initialIconState = dmeItem.getVarText(VAR_ICON_STATE) ?: ""

                dmi.iconStates.keys.filter { it != initialIconState }.let { iconStates ->
                    if (iconStates.isNotEmpty()) {
                        iconStates.forEach { iconStateName ->
                            GlobalTileItemHolder.getOrCreate(itemType, mutableMapOf(VAR_ICON_STATE to "\"$iconStateName\""))
                        }

                        sendEvent(TriggerObjectPanelUi.Update())
                    }
                }
            })
        }
    }

    private fun handleGenerateInstancesFromDirections(event: Event<TileItem, Unit>) {
        val tileItem = event.body
        GlobalDmiHolder.getIconState(tileItem.icon, tileItem.iconState)?.let { iconState ->
            sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment { dme ->
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

                sendEvent(TriggerObjectPanelUi.Update())
            })
        }
    }

    private fun handleFindInstancePositionsByType(event: Event<Pair<MapArea, String>, List<Pair<TileItem, MapPos>>>) {
        val positions = mutableListOf<Pair<TileItem, MapPos>>()

        sendEvent(TriggerMapHolderService.FetchSelectedMap { map ->
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

        sendEvent(TriggerMapHolderService.FetchSelectedMap { map ->
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

    private fun handleDeleteInstance(event: Event<TileItem, Unit>) {
        sendEvent(TriggerMapHolderService.FetchSelectedMap { map ->
            sendEvent(TriggerActionService.BatchActions { batchCompleteCallback ->
                sendEvent(TriggerInstanceService.FindInstancePositionsById(Pair(map.getMapArea(), event.body.id)) { instancePositions ->
                    val question = "Do you really want to delete an instance? (found in ${instancePositions.size} places)"
                    val confirmationDialogData = ConfirmationDialogData(question = question)

                    sendEvent(TriggerConfirmationDialogUi.Open(confirmationDialogData) {
                        if (it == ConfirmationDialogStatus.YES) {
                            sendEvent(TriggerMapModifierService.DeleteTileItemsWithIdInPositions(instancePositions))

                            if (!event.body.isDefaultInstance()) {
                                GlobalTileItemHolder.remove(event.body)

                                sendEvent(TriggerActionService.AddAction(TileItemAddAction(event.body) {
                                    sendEvent(TriggerObjectPanelUi.Update())
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
