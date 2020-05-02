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

    var currentSelectedArea: MapArea = MapArea.OUT_OF_BOUNDS_AREA
    private var initialArea: MapArea = currentSelectedArea

    override fun onStart(mapPos: MapPos) {
        isActive = true

        if (isActive) {
            prevMapPosX = mapPos.x
            prevMapPosY = mapPos.y

            initialArea = currentSelectedArea

            sendEvent(TriggerLayersFilterController.FetchFilteredLayers { filteredTypes ->
                this.filteredTypes = filteredTypes // We need to know, which layers were filtered at the beginning

                // Save initial tile items
                sendEvent(TriggerMapHolderController.FetchSelectedMap { selectedMap ->
                    for (x in currentSelectedArea.x1..currentSelectedArea.x2) {
                        for (y in currentSelectedArea.y1..currentSelectedArea.y2) {
                            val filteredTileItems = selectedMap.getTile(x, y, selectedMap.zSelected).getFilteredTileItems(filteredTypes)
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
            sendEvent(TriggerMapHolderController.FetchSelectedMap { selectedMap ->
                // Restore initial tiles state and create a reverse action
                for (x in initialArea.x1..initialArea.x2) {
                    for (y in initialArea.y1..initialArea.y2) {
                        val tile = selectedMap.getTile(x, y, selectedMap.zSelected)
                        var movedTileItems: List<TileItem>? = null

                        // If it's a part of the selected area, then it filled with moved items
                        if (currentSelectedArea.isInBounds(x, y)) {
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
            sendEvent(TriggerActionController.AddAction(MultiAction(reverseActions.toList())))
            sendEvent(TriggerFrameController.RefreshFrame())
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

        val x1 = currentSelectedArea.x1 + xAxisShift
        val y1 = currentSelectedArea.y1 + yAxisShift
        val x2 = currentSelectedArea.x2 + xAxisShift
        val y2 = currentSelectedArea.y2 + yAxisShift

        prevMapPosX = mapPos.x
        prevMapPosY = mapPos.y

        sendEvent(TriggerMapHolderController.FetchSelectedMap { selectedMap ->
            if (x1 !in 1..selectedMap.maxX || y1 !in 1..selectedMap.maxY || x2 !in 1..selectedMap.maxX || y2 !in 1..selectedMap.maxY) {
                return@FetchSelectedMap
            }

            // Restore tile items for tiles we left out of the selection
            for (x in currentSelectedArea.x1..currentSelectedArea.x2) {
                for (y in currentSelectedArea.y1..currentSelectedArea.y2) {
                    val tile = selectedMap.getTile(x, y, selectedMap.zSelected)

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

            currentSelectedArea = MapArea(x1, y1, x2, y2)

            for (x in currentSelectedArea.x1..currentSelectedArea.x2) {
                for (y in currentSelectedArea.y1..currentSelectedArea.y2) {
                    val tile = selectedMap.getTile(x, y, selectedMap.zSelected)
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

            sendEvent(TriggerFrameController.RefreshFrame())
            sendEvent(TriggerCanvasController.SelectArea(currentSelectedArea))
        })
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        // unused
    }

    override fun getSelectedArea(): MapArea = currentSelectedArea

    override fun reset() {
        onStop()
        currentSelectedArea = MapArea.OUT_OF_BOUNDS_AREA
        sendEvent(TriggerCanvasController.ResetSelectedArea())
    }

    override fun destroy() {
        reset()
    }

    private fun TIntObjectHashMap<TIntObjectHashMap<List<TileItem>>>.put(x: Int, y: Int, tileItems: List<TileItem>) {
        this.getOrPut(x) { TIntObjectHashMap() }.put(y, tileItems)
    }
}
