package strongdmm.controller

import strongdmm.byond.*
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.*
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.ui.search.SearchRect

class InstanceController : EventConsumer, EventSender {
    init {
        consumeEvent(Event.InstanceController.GenerateFromIconStates::class.java, ::handleGenerateFromIconStates)
        consumeEvent(Event.InstanceController.GenerateFromDirections::class.java, ::handleGenerateFromDirections)
        consumeEvent(Event.InstanceController.FindPositionsByType::class.java, ::handleFindPositionsByType)
        consumeEvent(Event.InstanceController.FindPositionsById::class.java, ::handleFindPositionsById)
    }

    private fun handleGenerateFromIconStates(event: Event<TileItem, Unit>) {
        GlobalDmiHolder.getDmi(event.body.icon)?.let { dmi ->
            sendEvent(Event.EnvironmentController.Fetch { dme ->
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

    private fun handleGenerateFromDirections(event: Event<TileItem, Unit>) {
        val tileItem = event.body
        GlobalDmiHolder.getIconState(tileItem.icon, tileItem.iconState)?.let { iconState ->
            sendEvent(Event.EnvironmentController.Fetch { dme ->
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

    private fun handleFindPositionsByType(event: Event<Pair<SearchRect, TileItemType>, List<Pair<TileItemType, MapPos>>>) {
        val positions = mutableListOf<Pair<TileItemType, MapPos>>()

        sendEvent(Event.MapController.FetchSelected { map ->
            if (map != null && event.body.second.isNotEmpty()) {
                val (x1, y1, x2, y2) = event.body.first
                for (x in (x1..x2)) {
                    for (y in (y1..y2)) {
                        map.getTileItemsId(x, y).forEach { tileItemId ->
                            val tileItem = GlobalTileItemHolder.getById(tileItemId)
                            if (tileItem.type.contains(event.body.second)) {
                                positions.add(Pair(tileItem.type, MapPos(x, y)))
                            }
                        }
                    }
                }
            }
        })

        event.reply(positions)
    }

    private fun handleFindPositionsById(event: Event<Pair<SearchRect, TileItemId>, List<Pair<TileItemType, MapPos>>>) {
        val positions = mutableListOf<Pair<TileItemType, MapPos>>()

        sendEvent(Event.MapController.FetchSelected { map ->
            if (map != null) {
                val (x1, y1, x2, y2) = event.body.first
                for (x in (x1..x2)) {
                    for (y in (y1..y2)) {
                        map.getTileItemsId(x, y).forEach { tileItemId ->
                            val tileItem = GlobalTileItemHolder.getById(tileItemId)
                            if (tileItem.id == event.body.second) {
                                positions.add(Pair(tileItem.type, MapPos(x, y)))
                            }
                        }
                    }
                }
            }
        })

        event.reply(positions)
    }
}
