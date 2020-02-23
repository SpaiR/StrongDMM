package strongdmm.controller.tool

import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.event.Event
import strongdmm.event.EventSender
import kotlin.math.max
import kotlin.math.min

class FillDeleteTool : Tool(), EventSender {
    private var xStart: Int = 0
    private var yStart: Int = 0

    private var x1: Int = 0
    private var y1: Int = 0
    private var x2: Int = 0
    private var y2: Int = 0

    private var tileItemTypeToDelete: String? = null
    private var currentMap: Dmm? = null

    override fun onStart(mapPos: MapPos) {
        isActive = currentMap != null && tileItemTypeToDelete != null

        if (isActive) {
            xStart = mapPos.x
            yStart = mapPos.y
            fillAreaRect(mapPos.x, mapPos.y)
        }
    }

    override fun onStop() {
        isActive = false

        val reverseActions = mutableListOf<Undoable>()

        sendEvent(Event.LayersFilterController.Fetch { filteredTypes ->
            for (x in x1..x2) {
                for (y in y1..y2) {
                    currentMap?.getTile(x, y)?.let { tile ->
                        tile.getFilteredTileItems(filteredTypes).findLast { it.isType(tileItemTypeToDelete!!) }?.let { tileItem ->
                            reverseActions.add(ReplaceTileAction(tile) {
                                tile.deleteTileItem(tileItem)
                            })
                        }
                    }
                }
            }
        })

        if (reverseActions.isNotEmpty()) {
            sendEvent(Event.ActionController.AddAction(MultiAction(reverseActions)))
            sendEvent(Event.Global.RefreshFrame())
        }

        sendEvent(Event.CanvasController.ResetSelectedArea())
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        fillAreaRect(mapPos.x, mapPos.y)
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        tileItemTypeToDelete = when {
            tileItem == null -> ""
            tileItem.isType(TYPE_AREA) -> TYPE_AREA
            tileItem.isType(TYPE_TURF) -> TYPE_TURF
            tileItem.isType(TYPE_OBJ) -> TYPE_OBJ
            tileItem.isType(TYPE_MOB) -> TYPE_MOB
            else -> throw IllegalStateException("Unknown tile item type - ${tileItem.type}")
        }
    }

    override fun onMapSwitch(map: Dmm?) {
        currentMap = map
    }

    private fun fillAreaRect(x: Int, y: Int) {
        x1 = min(xStart, x)
        y1 = min(yStart, y)
        x2 = max(xStart, x)
        y2 = max(yStart, y)
        sendEvent(Event.CanvasController.SelectArea(Pair(MapPos(x1, y1), MapPos(x2, y2))))
    }
}
