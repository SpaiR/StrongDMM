package strongdmm.controller

import strongdmm.byond.*
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.*
import strongdmm.event.type.controller.EventEnvironmentController
import strongdmm.event.type.controller.EventInstanceController
import strongdmm.event.type.controller.EventMapHolderController
import strongdmm.ui.search.SearchRect

class InstanceController : EventConsumer, EventSender {
    init {
        consumeEvent(EventInstanceController.GenerateFromIconStates::class.java, ::handleGenerateFromIconStates)
        consumeEvent(EventInstanceController.GenerateFromDirections::class.java, ::handleGenerateFromDirections)
        consumeEvent(EventInstanceController.FindPositionsByType::class.java, ::handleFindPositionsByType)
        consumeEvent(EventInstanceController.FindPositionsById::class.java, ::handleFindPositionsById)
    }

    private fun handleGenerateFromIconStates(event: Event<TileItem, Unit>) {
        GlobalDmiHolder.getDmi(event.body.icon)?.let { dmi ->
            sendEvent(EventEnvironmentController.Fetch { dme ->
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
            sendEvent(EventEnvironmentController.Fetch { dme ->
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

    private fun handleFindPositionsByType(event: Event<Pair<SearchRect, TileItemType>, List<Pair<TileItem, MapPos>>>) {
        val positions = mutableListOf<Pair<TileItem, MapPos>>()

        sendEvent(EventMapHolderController.FetchSelected { map ->
            if (event.body.second.isNotEmpty()) {
                val (x1, y1, x2, y2) = event.body.first
                for (x in (x1..x2)) {
                    for (y in (y1..y2)) {
                        map.getTileItemsId(x, y).forEach { tileItemId ->
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

    private fun handleFindPositionsById(event: Event<Pair<SearchRect, TileItemId>, List<Pair<TileItem, MapPos>>>) {
        val positions = mutableListOf<Pair<TileItem, MapPos>>()

        sendEvent(EventMapHolderController.FetchSelected { map ->
            val (x1, y1, x2, y2) = event.body.first

            for (x in (x1..x2)) {
                for (y in (y1..y2)) {
                    map.getTileItemsId(x, y).forEach { tileItemId ->
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
