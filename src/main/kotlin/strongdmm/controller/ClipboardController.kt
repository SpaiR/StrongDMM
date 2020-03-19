package strongdmm.controller

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.*
import strongdmm.util.OUT_OF_BOUNDS

class ClipboardController : EventConsumer, EventSender {
    private var tileItems: Array<Array<List<TileItem>>>? = null
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    init {
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(EventClipboardController.Cut::class.java, ::handleCut)
        consumeEvent(EventClipboardController.Copy::class.java, ::handleCopy)
        consumeEvent(EventClipboardController.Paste::class.java, ::handlePaste)
    }

    private fun handleEnvironmentReset() {
        tileItems = null
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
    }

    private fun handleCut() {
        handleCopy()
        sendEvent(EventMapModifierController.DeleteActiveAreaTileItems())
    }

    private fun handleCopy() {
        sendEvent(EventMapHolderController.FetchSelected { selectedMap ->
            sendEvent(EventLayersFilterController.Fetch { filteredLayers ->
                sendEvent(EventToolsController.FetchActiveArea { activeArea ->
                    val width = activeArea.x2 - activeArea.x1 + 1
                    val height = activeArea.y2 - activeArea.y1 + 1
                    val tileItems = Array(width) { Array(height) { emptyList<TileItem>() } }

                    for ((xLocal, x) in (activeArea.x1..activeArea.x2).withIndex()) {
                        for ((yLocal, y) in (activeArea.y1..activeArea.y2).withIndex()) {
                            val tile = selectedMap.getTile(x, y)
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
            sendEvent(EventMapModifierController.ReplaceActiveAreaTileItems(tileItems!!))
        }
    }
}
