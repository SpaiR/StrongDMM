package strongdmm.controller.map

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.TileItemType
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.*
import strongdmm.util.OUT_OF_BOUNDS

class MapModifierController : EventConsumer, EventSender {
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    init {
        consumeEvent(EventGlobal.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(EventMapModifierController.DeleteActiveAreaTileItems::class.java, ::handleDeleteActiveAreaTileItems)
        consumeEvent(EventMapModifierController.FillCurrentMapPosWithTileItems::class.java, ::handleFillCurrentMapPosWithTileItems)
        consumeEvent(EventMapModifierController.ReplaceTypeInPositions::class.java, ::handleReplaceTypeInPositions)
        consumeEvent(EventMapModifierController.ReplaceIdInPositions::class.java, ::handleReplaceIdInPositions)
        consumeEvent(EventMapModifierController.DeleteTypeInPositions::class.java, ::handleDeleteTypeInPositions)
        consumeEvent(EventMapModifierController.DeleteIdInPositions::class.java, ::handleDeleteIdInPositions)
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
    }

    private fun handleDeleteActiveAreaTileItems() {
        sendEvent(EventMapHolderController.FetchSelected { selectedMap ->
            sendEvent(EventLayersFilterController.Fetch { filteredLayers ->
                sendEvent(EventToolsController.FetchActiveArea { activeArea ->
                    val reverseActions = mutableListOf<Undoable>()

                    for (x in (activeArea.x1..activeArea.x2)) {
                        for (y in (activeArea.y1..activeArea.y2)) {
                            val tile = selectedMap.getTile(x, y)

                            tile.getFilteredTileItems(filteredLayers).let { filteredTileItems ->
                                reverseActions.add(ReplaceTileAction(tile) {
                                    filteredTileItems.forEach { tileItem ->
                                        tile.deleteTileItem(tileItem)
                                    }
                                })
                            }
                        }
                    }

                    if (reverseActions.isNotEmpty()) {
                        sendEvent(EventActionController.AddAction(MultiAction(reverseActions)))
                        sendEvent(EventFrameController.Refresh())
                    }
                })
            })
        })
    }

    private fun handleFillCurrentMapPosWithTileItems(event: Event<Array<Array<List<TileItem>>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelected { selectedMap ->
            sendEvent(EventLayersFilterController.Fetch { filteredLayers ->
                val reverseActions = mutableListOf<Undoable>()

                for ((x, col) in event.body.withIndex()) {
                    for ((y, tileItems) in col.withIndex()) {
                        val xPos = currentMapPos.x + x
                        val yPos = currentMapPos.y + y

                        if (xPos !in 1..selectedMap.maxX || yPos !in 1..selectedMap.maxY) {
                            continue
                        }

                        val tile = selectedMap.getTile(currentMapPos.x + x, currentMapPos.y + y)

                        reverseActions.add(ReplaceTileAction(tile) {
                            tile.getFilteredTileItems(filteredLayers).forEach { tileItem ->
                                tile.deleteTileItem(tileItem)
                            }

                            tileItems.forEach {
                                tile.addTileItem(it)
                            }
                        })
                    }
                }

                if (reverseActions.isNotEmpty()) {
                    sendEvent(EventActionController.AddAction(MultiAction(reverseActions)))
                    sendEvent(EventFrameController.Refresh())
                }
            })
        })
    }

    private fun handleReplaceTypeInPositions(event: Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelected { dmm ->
            val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first)
            val replaceActions = mutableListOf<Undoable>()

            event.body.second.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                replaceActions.add(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem.type, replaceWithTileItem)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(replaceActions)))
            sendEvent(EventFrameController.Refresh())
        })
    }

    private fun handleReplaceIdInPositions(event: Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelected { dmm ->
            val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first)
            val replaceActions = mutableListOf<Undoable>()

            event.body.second.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                replaceActions.add(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem.id, replaceWithTileItem)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(replaceActions)))
            sendEvent(EventFrameController.Refresh())
        })
    }

    private fun handleDeleteTypeInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelected { dmm ->
            val deleteActions = mutableListOf<Undoable>()

            event.body.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                deleteActions.add(ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem.type)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(deleteActions)))
            sendEvent(EventFrameController.Refresh())
        })
    }

    private fun handleDeleteIdInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelected { dmm ->
            val deleteActions = mutableListOf<Undoable>()

            event.body.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                deleteActions.add(ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem.id)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(deleteActions)))
            sendEvent(EventFrameController.Refresh())
        })
    }
}
