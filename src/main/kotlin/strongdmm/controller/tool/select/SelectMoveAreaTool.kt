package strongdmm.controller.tool.select

import gnu.trove.map.hash.TIntObjectHashMap
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.controller.tool.Tool
import strongdmm.event.EventSender
import strongdmm.event.type.controller.*
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.extension.getOrPut

class SelectMoveAreaTool : Tool(), EventSender {
    private var prevMapPosX: Int = 0
    private var prevMapPosY: Int = 0

    private var totalXShift: Int = 0
    private var totalYShift: Int = 0

    private var tilesItemsToMove: TIntObjectHashMap<TIntObjectHashMap<List<TileItem>>> = TIntObjectHashMap()
    private var tilesItemsStored: TIntObjectHashMap<TIntObjectHashMap<List<TileItem>>> = TIntObjectHashMap()

    private val reverseActions: MutableList<Undoable> = mutableListOf()

    private var filteredTypes: Set<String> = emptySet()

    var selectedArea: MapArea = MapArea(OUT_OF_BOUNDS, OUT_OF_BOUNDS, OUT_OF_BOUNDS, OUT_OF_BOUNDS)
    private var initialArea: MapArea = selectedArea

    override fun onStart(mapPos: MapPos) {
        isActive = true

        if (isActive) {
            prevMapPosX = mapPos.x
            prevMapPosY = mapPos.y

            initialArea = selectedArea

            sendEvent(EventLayersFilterController.Fetch { filteredTypes ->
                this.filteredTypes = filteredTypes // We need to know, which layers were filtered at the beginning

                // Save initial tile items
                sendEvent(EventMapHolderController.FetchSelected { selectedMap ->
                    for (x in selectedArea.x1..selectedArea.x2) {
                        for (y in selectedArea.y1..selectedArea.y2) {
                            val filteredTileItems = selectedMap.getTile(x, y).getFilteredTileItems(filteredTypes)
                            tilesItemsToMove.put(x, y, filteredTileItems)
                            tilesItemsStored.put(x, y, filteredTileItems)
                        }
                    }
                })
            })
        }
    }

    override fun onStop() {
        isActive = false

        if (!tilesItemsToMove.isEmpty) {
            sendEvent(EventMapHolderController.FetchSelected { selectedMap ->
                // Restore initial tiles state and create a reverse action
                for (x in initialArea.x1..initialArea.x2) {
                    for (y in initialArea.y1..initialArea.y2) {
                        val tile = selectedMap.getTile(x, y)
                        var movedTileItems: List<TileItem>? = null

                        // If it's a part of the selected area, then it filled with moved items
                        if (selectedArea.isInBounds(x, y)) {
                            movedTileItems = tile.getFilteredTileItems(filteredTypes)
                            movedTileItems.forEach { tileIem ->
                                tile.deleteTileItem(tileIem) // So we delete them..
                            }
                        }

                        val initialTileItems = tilesItemsToMove[x][y]

                        // ..and restore initial items to create a proper reverse action for them
                        initialTileItems.forEach { tileItem ->
                            tile.addTileItem(tileItem)
                        }

                        reverseActions.add(ReplaceTileAction(tile) {
                            initialTileItems.forEach { tileItem ->
                                tile.deleteTileItem(tileItem)
                            }
                        })

                        // Once reverse action was created, if there were any moved items - we restore them
                        movedTileItems?.forEach {
                            tile.addTileItem(it)
                        }
                    }
                }
            })
        }

        if (reverseActions.isNotEmpty()) {
            sendEvent(EventActionController.AddAction(MultiAction(reverseActions.toList())))
            sendEvent(EventFrameController.Refresh())
        }

        tilesItemsToMove.clear()
        tilesItemsStored.clear()
        reverseActions.clear()
        totalXShift = 0
        totalYShift = 0
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        val xAxisShift = mapPos.x - prevMapPosX
        val yAxisShift = mapPos.y - prevMapPosY

        val x1 = selectedArea.x1 + xAxisShift
        val y1 = selectedArea.y1 + yAxisShift
        val x2 = selectedArea.x2 + xAxisShift
        val y2 = selectedArea.y2 + yAxisShift

        prevMapPosX = mapPos.x
        prevMapPosY = mapPos.y

        sendEvent(EventMapHolderController.FetchSelected { selectedMap ->
            if (x1 !in 1..selectedMap.maxX || y1 !in 1..selectedMap.maxY || x2 !in 1..selectedMap.maxX || y2 !in 1..selectedMap.maxY) {
                return@FetchSelected
            }

            // Restore tile items for tiles we left out of the selection
            for (x in selectedArea.x1..selectedArea.x2) {
                for (y in selectedArea.y1..selectedArea.y2) {
                    val tile = selectedMap.getTile(x, y)

                    tile.getFilteredTileItems(filteredTypes).forEach { tileIem ->
                        tile.deleteTileItem(tileIem)
                    }

                    if (!initialArea.isInBounds(x, y)) {
                        tilesItemsStored[x][y].forEach { tileItem ->
                            tile.addTileItem(tileItem)
                        }
                    }
                }
            }

            tilesItemsStored.clear()
            reverseActions.clear()

            totalXShift += xAxisShift
            totalYShift += yAxisShift

            selectedArea = MapArea(x1, y1, x2, y2)

            for (x in selectedArea.x1..selectedArea.x2) {
                for (y in selectedArea.y1..selectedArea.y2) {
                    val tile = selectedMap.getTile(x, y)
                    val filteredTileItems = tile.getFilteredTileItems(filteredTypes)

                    tilesItemsStored.put(x, y, filteredTileItems)

                    val replaceTileAction = ReplaceTileAction(tile) {
                        filteredTileItems.forEach { tileItem ->
                            tile.deleteTileItem(tileItem)
                        }

                        val xInit = x - totalXShift
                        val yInit = y - totalYShift
                        val tileItems = tilesItemsToMove[xInit][yInit]

                        tileItems.forEach { tileItem ->
                            tile.addTileItem(tileItem)
                        }
                    }

                    // For initial area we create a separate reverse actions
                    if (!initialArea.isInBounds(x, y)) {
                        reverseActions.add(replaceTileAction)
                    }
                }
            }

            sendEvent(EventFrameController.Refresh())
            sendEvent(EventCanvasController.SelectArea(selectedArea))
        })
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        // unused
    }

    override fun getActiveArea(): MapArea = selectedArea

    override fun reset() {
        onStop()
        sendEvent(EventCanvasController.ResetSelectedArea())
    }

    override fun destroy() {
        reset()
    }

    private fun TIntObjectHashMap<TIntObjectHashMap<List<TileItem>>>.put(x: Int, y: Int, tileItems: List<TileItem>) {
        this.getOrPut(x) { TIntObjectHashMap() }.put(y, tileItems)
    }
}
