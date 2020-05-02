package strongdmm.service

import strongdmm.byond.*
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.controller.TriggerEnvironmentController
import strongdmm.event.type.controller.TriggerInstanceController
import strongdmm.event.type.controller.TriggerMapHolderController

class InstanceService : EventConsumer, EventSender {
    init {
        consumeEvent(TriggerInstanceController.GenerateInstancesFromIconStates::class.java, ::handleGenerateInstancesFromIconStates)
        consumeEvent(TriggerInstanceController.GenerateInstancesFromDirections::class.java, ::handleGenerateInstancesFromDirections)
        consumeEvent(TriggerInstanceController.FindInstancePositionsByType::class.java, ::handleFindInstancePositionsByType)
        consumeEvent(TriggerInstanceController.FindInstancePositionsById::class.java, ::handleFindInstancePositionsById)
    }

    private fun handleGenerateInstancesFromIconStates(event: Event<TileItem, Unit>) {
        GlobalDmiHolder.getDmi(event.body.icon)?.let { dmi ->
            sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment { dme ->
                val itemType = event.body.type
                val dmeItem = dme.getItem(itemType)!!
                val initialIconState = dmeItem.getVarText(VAR_ICON_STATE) ?: ""

                dmi.iconStates.keys.filter { it != initialIconState }.let { iconStates ->
                    if (iconStates.isNotEmpty()) {
                        iconStates.forEach { iconStateName ->
                            GlobalTileItemHolder.getOrCreate(itemType, mutableMapOf(VAR_ICON_STATE to "\"$iconStateName\""))
                        }
                        event.reply(Unit)
                    }
                }
            })
        }
    }

    private fun handleGenerateInstancesFromDirections(event: Event<TileItem, Unit>) {
        val tileItem = event.body
        GlobalDmiHolder.getIconState(tileItem.icon, tileItem.iconState)?.let { iconState ->
            sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment { dme ->
                val dmeItem = dme.getItem(tileItem.type)!!
                val initialDir = dmeItem.getVarInt(VAR_DIR) ?: DEFAULT_DIR

                when (iconState.dirs) {
                    4 -> {
                        arrayOf(NORTH, SOUTH, EAST, WEST).filter { it != initialDir }.forEach { dir ->
                            GlobalTileItemHolder.getOrCreate(tileItem.type, mutableMapOf(VAR_DIR to dir.toString()))
                        }
                        event.reply(Unit)
                    }
                    8 -> {
                        arrayOf(NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST).filter { it != initialDir }.forEach { dir ->
                            GlobalTileItemHolder.getOrCreate(tileItem.type, mutableMapOf(VAR_DIR to dir.toString()))
                        }
                        event.reply(Unit)
                    }
                }
            })
        }
    }

    private fun handleFindInstancePositionsByType(event: Event<Pair<MapArea, String>, List<Pair<TileItem, MapPos>>>) {
        val positions = mutableListOf<Pair<TileItem, MapPos>>()

        sendEvent(TriggerMapHolderController.FetchSelectedMap { map ->
            if (event.body.second.isNotEmpty()) {
                val (x1, y1, x2, y2) = event.body.first
                for (x in (x1..x2)) {
                    for (y in (y1..y2)) {
                        map.getTileItemsId(x, y, map.zSelected).forEach { tileItemId ->
                            val tileItem = GlobalTileItemHolder.getById(tileItemId)
                            if (tileItem.type == event.body.second) {
                                positions.add(Pair(tileItem, MapPos(x, y)))
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

        sendEvent(TriggerMapHolderController.FetchSelectedMap { map ->
            val (x1, y1, x2, y2) = event.body.first

            for (x in (x1..x2)) {
                for (y in (y1..y2)) {
                    map.getTileItemsId(x, y, map.zSelected).forEach { tileItemId ->
                        val tileItem = GlobalTileItemHolder.getById(tileItemId)
                        if (tileItem.id == event.body.second) {
                            positions.add(Pair(tileItem, MapPos(x, y)))
                        }
                    }
                }
            }
        })

        event.reply(positions)
    }
}
