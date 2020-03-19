package strongdmm.controller

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
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
        sendEvent(EventMapHolderController.FetchSelected { selectedMap ->
            sendEvent(EventLayersFilterController.Fetch { filteredLayers ->
                sendEvent(EventToolsController.FetchActiveArea { activeArea ->
                    val width = activeArea.x2 - activeArea.x1 + 1
                    val height = activeArea.y2 - activeArea.y1 + 1
                    val tileItems = Array(width) { Array(height) { emptyList<TileItem>() } }
                    val reverseActions = mutableListOf<Undoable>()

                    for ((xLocal, x) in (activeArea.x1..activeArea.x2).withIndex()) {
                        for ((yLocal, y) in (activeArea.y1..activeArea.y2).withIndex()) {
                            val tile = selectedMap.getTile(x, y)

                            tile.getFilteredTileItems(filteredLayers).let { filteredTileItems ->
                                tileItems[xLocal][yLocal] = filteredTileItems

                                reverseActions.add(ReplaceTileAction(tile) {
                                    filteredTileItems.forEach { tileItem ->
                                        tile.deleteTileItem(tileItem)
                                    }
                                })
                            }
                        }
                    }

                    this.tileItems = tileItems

                    if (reverseActions.isNotEmpty()) {
                        sendEvent(EventActionController.AddAction(MultiAction(reverseActions)))
                        sendEvent(EventFrameController.Refresh())
                    }
                })
            })
        })
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
        if (currentMapPos.isOutOfBounds() || tileItems == null) {
            return
        }

        sendEvent(EventMapHolderController.FetchSelected { selectedMap ->
            sendEvent(EventLayersFilterController.Fetch { filteredLayers ->
                val reverseActions = mutableListOf<Undoable>()

                for ((x, col) in tileItems!!.withIndex()) {
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
}
