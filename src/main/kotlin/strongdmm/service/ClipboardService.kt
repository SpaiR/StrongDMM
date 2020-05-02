package strongdmm.service

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.*
import strongdmm.util.OUT_OF_BOUNDS

class ClipboardService : EventHandler {
    private var tileItems: Array<Array<List<TileItem>>>? = null
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    init {
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(TriggerClipboardService.Cut::class.java, ::handleCut)
        consumeEvent(TriggerClipboardService.Copy::class.java, ::handleCopy)
        consumeEvent(TriggerClipboardService.Paste::class.java, ::handlePaste)
    }

    private fun handleEnvironmentReset() {
        tileItems = null
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
    }

    private fun handleCut() {
        sendEvent(TriggerClipboardService.Copy())
        sendEvent(TriggerMapModifierService.DeleteTileItemsInSelectedArea())
    }

    private fun handleCopy() {
        sendEvent(TriggerMapHolderService.FetchSelectedMap { selectedMap ->
            sendEvent(TriggerLayersFilterService.FetchFilteredLayers { filteredLayers ->
                sendEvent(TriggerToolsService.FetchSelectedArea { selectedArea ->
                    val width = selectedArea.x2 - selectedArea.x1 + 1
                    val height = selectedArea.y2 - selectedArea.y1 + 1
                    val tileItems = Array(width) { Array(height) { emptyList<TileItem>() } }

                    for ((xLocal, x) in (selectedArea.x1..selectedArea.x2).withIndex()) {
                        for ((yLocal, y) in (selectedArea.y1..selectedArea.y2).withIndex()) {
                            val tile = selectedMap.getTile(x, y, selectedMap.zSelected)
                            tileItems[xLocal][yLocal] = tile.getFilteredTileItems(filteredLayers)
                        }
                    }

                    this.tileItems = tileItems
                })
            })
        })
    }

    private fun handlePaste() {
        if (!currentMapPos.isOutOfBounds() && tileItems != null) {
            sendEvent(TriggerMapModifierService.FillSelectedMapPositionWithTileItems(tileItems!!))
        }
    }
}
