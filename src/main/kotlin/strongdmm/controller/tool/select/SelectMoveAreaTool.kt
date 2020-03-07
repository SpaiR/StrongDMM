package strongdmm.controller.tool.select

import gnu.trove.map.hash.TIntObjectHashMap
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.controller.tool.Tool
import strongdmm.event.Event
import strongdmm.event.EventSender
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.extension.getOrPut

class SelectMoveAreaTool : Tool(), EventSender {
    private var currentX: Int = 0
    private var currentY: Int = 0

    private var totalXShift: Int = 0
    private var totalYShift: Int = 0
    private var tilesItemsToMove: TIntObjectHashMap<TIntObjectHashMap<List<TileItem>>> = TIntObjectHashMap()
    private var filteredTypes: Set<String> = emptySet()

    var selectedArea: MapArea = MapArea(OUT_OF_BOUNDS, OUT_OF_BOUNDS, OUT_OF_BOUNDS, OUT_OF_BOUNDS)
    private var initialArea: MapArea = selectedArea

    private var currentMap: Dmm? = null

    override fun onStart(mapPos: MapPos) {
        isActive = currentMap != null

        if (isActive) {
            currentX = mapPos.x
            currentY = mapPos.y

            initialArea = selectedArea

            sendEvent(Event.LayersFilterController.Fetch { filteredTypes ->
                this.filteredTypes = filteredTypes

                for (x in selectedArea.x1..selectedArea.x2) {
                    for (y in selectedArea.y1..selectedArea.y2) {
                        tilesItemsToMove.getOrPut(x) { TIntObjectHashMap() }.getOrPut(y) { currentMap!!.getTile(x, y).getFilteredTileItems(filteredTypes) }
                    }
                }
            })
        }
    }

    override fun onStop() {
        isActive = false

        val reverseActions = mutableListOf<Undoable>()

        // Delete moved tile items from the initial location (if it's not the part of the selected area)
        for (x in initialArea.x1..initialArea.x2) {
            for (y in initialArea.y1..initialArea.y2) {
                val tileItems = tilesItemsToMove[x][y]
                val tileToMove = currentMap!!.getTile(x, y)

                if (!selectedArea.isInBounds(x, y)) {
                    reverseActions.add(ReplaceTileAction(tileToMove) {
                        tileItems.forEach { tileItem ->
                            tileToMove.deleteTileItem(tileItem)
                        }
                    })
                }
            }
        }

        // Replace tile items in the selected location with tile items from the original one
        for (x in selectedArea.x1..selectedArea.x2) {
            for (y in selectedArea.y1..selectedArea.y2) {
                val xInit = x - totalXShift
                val yInit = y - totalYShift
                val tileItems = tilesItemsToMove[xInit][yInit]
                val tileToReplace = currentMap!!.getTile(x, y)

                reverseActions.add(ReplaceTileAction(tileToReplace) {
                    tileToReplace.tileItems.toList().forEach {
                        if (it.type !in filteredTypes) {
                            tileToReplace.deleteTileItem(it)
                        }
                    }

                    tileItems.forEach { tileItem ->
                        tileToReplace.addTileItem(tileItem)
                    }
                })
            }
        }

        if (reverseActions.isNotEmpty()) {
            sendEvent(Event.ActionController.AddAction(MultiAction(reverseActions)))
            sendEvent(Event.Global.RefreshFrame())
        }

        tilesItemsToMove.clear()
        totalXShift = 0
        totalYShift = 0
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        val xAxisShift = mapPos.x - currentX
        val yAxisShift = mapPos.y - currentY

        val x1 = selectedArea.x1 + xAxisShift
        val y1 = selectedArea.y1 + yAxisShift
        val x2 = selectedArea.x2 + xAxisShift
        val y2 = selectedArea.y2 + yAxisShift

        if ((x1 !in 0..currentMap!!.maxX) || (y1 !in 0..currentMap!!.maxY) || (x2 !in 0..currentMap!!.maxX) || (y2 !in 0..currentMap!!.maxY)) {
            return
        }

        totalXShift += xAxisShift
        totalYShift += yAxisShift

        currentX = mapPos.x
        currentY = mapPos.y
        selectedArea = MapArea(x1, y1, x2, y2)

        sendEvent(Event.CanvasController.SelectArea(selectedArea))
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        // unused
    }

    override fun onMapSwitch(map: Dmm?) {
        currentMap = map
    }

    override fun reset() {
        isActive = false
        tilesItemsToMove.clear()
        totalXShift = 0
        totalYShift = 0
        sendEvent(Event.CanvasController.ResetSelectedArea())
    }

    override fun destroy() {
        reset()
        currentMap = null
    }
}
