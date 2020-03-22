package strongdmm.controller.map

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.MapArea
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
        consumeEvent(EventMapModifierController.DeleteTileItemsInActiveArea::class.java, ::handleDeleteTileItemsInActiveArea)
        consumeEvent(EventMapModifierController.FillSelectedMapPositionWithTileItems::class.java, ::handleFillSelectedMapPositionWithTileItems)
        consumeEvent(EventMapModifierController.ReplaceTileItemsWithTypeInPositions::class.java, ::handleReplaceTileItemsWithTypeInPositions)
        consumeEvent(EventMapModifierController.ReplaceTileItemsWithIdInPositions::class.java, ::handleReplaceTileItemsWithIdInPositions)
        consumeEvent(EventMapModifierController.DeleteTileItemsWithTypeInPositions::class.java, ::handleDeleteTileItemsWithTypeInPositions)
        consumeEvent(EventMapModifierController.DeleteTileItemsWithIdInPositions::class.java, ::handleDeleteTileItemsWithIdInPositions)
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
    }

    private fun handleDeleteTileItemsInActiveArea() {
        sendEvent(EventMapHolderController.FetchSelectedMap { selectedMap ->
            sendEvent(EventLayersFilterController.FetchFilteredLayers { filteredLayers ->
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
                        sendEvent(EventFrameController.RefreshFrame())
                    }
                })
            })
        })
    }

    private fun handleFillSelectedMapPositionWithTileItems(event: Event<Array<Array<List<TileItem>>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelectedMap { selectedMap ->
            sendEvent(EventLayersFilterController.FetchFilteredLayers { filteredLayers ->
                val reverseActions = mutableListOf<Undoable>()

                var x2 = currentMapPos.x
                var y2 = currentMapPos.y

                for ((x, col) in event.body.withIndex()) {
                    for ((y, tileItems) in col.withIndex()) {
                        val xPos = currentMapPos.x + x
                        val yPos = currentMapPos.y + y

                        if (xPos !in 1..selectedMap.maxX || yPos !in 1..selectedMap.maxY) {
                            continue
                        }

                        x2 = xPos
                        y2 = yPos

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
                    sendEvent(EventToolsController.SelectActiveArea(MapArea(currentMapPos.x, currentMapPos.y, x2, y2)))
                    sendEvent(EventFrameController.RefreshFrame())
                }
            })
        })
    }

    private fun handleReplaceTileItemsWithTypeInPositions(event: Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelectedMap { dmm ->
            val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first)
            val replaceActions = mutableListOf<Undoable>()

            event.body.second.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                replaceActions.add(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem.type, replaceWithTileItem)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(replaceActions)))
            sendEvent(EventFrameController.RefreshFrame())
        })
    }

    private fun handleReplaceTileItemsWithIdInPositions(event: Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelectedMap { dmm ->
            val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first)
            val replaceActions = mutableListOf<Undoable>()

            event.body.second.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                replaceActions.add(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem.id, replaceWithTileItem)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(replaceActions)))
            sendEvent(EventFrameController.RefreshFrame())
        })
    }

    private fun handleDeleteTileItemsWithTypeInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelectedMap { dmm ->
            val deleteActions = mutableListOf<Undoable>()

            event.body.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                deleteActions.add(ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem.type)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(deleteActions)))
            sendEvent(EventFrameController.RefreshFrame())
        })
    }

    private fun handleDeleteTileItemsWithIdInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelectedMap { dmm ->
            val deleteActions = mutableListOf<Undoable>()

            event.body.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                deleteActions.add(ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem.id)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(deleteActions)))
            sendEvent(EventFrameController.RefreshFrame())
        })
    }
}
