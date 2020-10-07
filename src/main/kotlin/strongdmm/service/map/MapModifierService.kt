package strongdmm.service.map

import strongdmm.application.Service
import strongdmm.byond.dmm.*
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.*
import strongdmm.service.action.undoable.MultiAction
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.service.action.undoable.Undoable
import strongdmm.util.OUT_OF_BOUNDS

class MapModifierService : Service {
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    init {
        EventBus.sign(Reaction.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        EventBus.sign(TriggerMapModifierService.DeleteTileItemsInSelectedArea::class.java, ::handleDeleteTileItemsInSelectedArea)
        EventBus.sign(TriggerMapModifierService.FillSelectedMapPositionWithTileItems::class.java, ::handleFillSelectedMapPositionWithTileItems)
        EventBus.sign(TriggerMapModifierService.ReplaceTileItemsWithTypeInPositions::class.java, ::handleReplaceTileItemsWithTypeInPositions)
        EventBus.sign(TriggerMapModifierService.ReplaceTileItemsWithIdInPositions::class.java, ::handleReplaceTileItemsWithIdInPositions)
        EventBus.sign(TriggerMapModifierService.ReplaceTileItemsByIdWithIdInPositions::class.java, ::handleReplaceTileItemsByIdWithIdInPositions)
        EventBus.sign(TriggerMapModifierService.DeleteTileItemsWithTypeInPositions::class.java, ::handleDeleteTileItemsWithTypeInPositions)
        EventBus.sign(TriggerMapModifierService.DeleteTileItemsWithIdInPositions::class.java, ::handleDeleteTileItemsWithIdInPositions)
        EventBus.sign(TriggerMapModifierService.ChangeMapSize::class.java, ::handleChangeMapSize)
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
    }

    private fun handleDeleteTileItemsInSelectedArea() {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { selectedMap ->
            EventBus.post(TriggerLayersFilterService.FetchFilteredLayers { filteredLayers ->
                EventBus.post(TriggerToolsService.FetchSelectedArea { selectedArea ->
                    val reverseActions = mutableListOf<Undoable>()

                    for (x in (selectedArea.x1..selectedArea.x2)) {
                        for (y in (selectedArea.y1..selectedArea.y2)) {
                            val tile = selectedMap.getTile(x, y, selectedMap.zSelected)
                            val initialTileItems = tile.getTileItemsId()

                            tile.getFilteredTileItems(filteredLayers).let { filteredTileItems ->
                                val replaceTileAction = ReplaceTileAction(tile) {
                                    filteredTileItems.forEach { tileItem ->
                                        tile.deleteTileItem(tileItem)
                                    }
                                }

                                if (!tile.getTileItemsId().contentEquals(initialTileItems)) {
                                    reverseActions.add(replaceTileAction)
                                }
                            }
                        }
                    }

                    if (reverseActions.isNotEmpty()) {
                        EventBus.post(TriggerActionService.QueueUndoable(MultiAction(reverseActions)))
                        EventBus.post(TriggerFrameService.RefreshFrame())
                    }
                })
            })
        })
    }

    private fun handleFillSelectedMapPositionWithTileItems(event: Event<Array<Array<List<TileItem>>>, Unit>) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { selectedMap ->
            EventBus.post(TriggerLayersFilterService.FetchFilteredLayers { filteredLayers ->
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

                        val tile = selectedMap.getTile(currentMapPos.x + x, currentMapPos.y + y, selectedMap.zSelected)

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
                    EventBus.post(TriggerActionService.QueueUndoable(MultiAction(reverseActions)))
                    EventBus.post(TriggerToolsService.SelectArea(MapArea(currentMapPos.x, currentMapPos.y, x2, y2)))
                    EventBus.post(TriggerFrameService.RefreshFrame())
                }
            })
        })
    }

    private fun handleReplaceTileItemsWithTypeInPositions(event: Event<Pair<String, List<Pair<TileItem, MapPos>>>, Unit>) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { dmm ->
            val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first)
            val replaceActions = mutableListOf<Undoable>()

            event.body.second.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y, pos.z)
                replaceActions.add(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem.type, replaceWithTileItem)
                })
            }

            EventBus.post(TriggerActionService.QueueUndoable(MultiAction(replaceActions)))
            EventBus.post(TriggerFrameService.RefreshFrame())
        })
    }

    private fun handleReplaceTileItemsByIdWithIdInPositions(event: Event<Pair<Long, List<Pair<TileItem, MapPos>>>, Unit>) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { dmm ->
            val replaceWithTileItem = GlobalTileItemHolder.getById(event.body.first)
            val replaceActions = mutableListOf<Undoable>()

            event.body.second.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y, pos.z)
                replaceActions.add(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem.type, replaceWithTileItem)
                })
            }

            EventBus.post(TriggerActionService.QueueUndoable(MultiAction(replaceActions)))
            EventBus.post(TriggerFrameService.RefreshFrame())
        })
    }

    private fun handleReplaceTileItemsWithIdInPositions(event: Event<Pair<String, List<Pair<TileItem, MapPos>>>, Unit>) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { dmm ->
            val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first)
            val replaceActions = mutableListOf<Undoable>()

            event.body.second.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y, pos.z)
                replaceActions.add(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem.id, replaceWithTileItem)
                })
            }

            EventBus.post(TriggerActionService.QueueUndoable(MultiAction(replaceActions)))
            EventBus.post(TriggerFrameService.RefreshFrame())
        })
    }

    private fun handleDeleteTileItemsWithTypeInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { dmm ->
            val deleteActions = mutableListOf<Undoable>()

            event.body.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y, pos.z)
                deleteActions.add(ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem.type)
                })
            }

            EventBus.post(TriggerActionService.QueueUndoable(MultiAction(deleteActions)))
            EventBus.post(TriggerFrameService.RefreshFrame())
        })
    }

    private fun handleDeleteTileItemsWithIdInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { dmm ->
            val deleteActions = mutableListOf<Undoable>()

            event.body.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y, pos.z)
                deleteActions.add(ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem.id)
                })
            }

            EventBus.post(TriggerActionService.QueueUndoable(MultiAction(deleteActions)))
            EventBus.post(TriggerFrameService.RefreshFrame())
        })
    }

    private fun handleChangeMapSize(event: Event<MapSize, Unit>) {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { dmm ->
            dmm.setMapSize(event.body.maxZ, event.body.maxY, event.body.maxX)
            EventBus.post(Reaction.SelectedMapMapSizeChanged(event.body))
        })
    }
}
