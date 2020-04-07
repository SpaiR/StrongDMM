package strongdmm.controller.frame

import strongdmm.byond.*
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerFrameController
import strongdmm.event.type.controller.TriggerLayersFilterController
import strongdmm.event.type.controller.TriggerMapHolderController
import strongdmm.util.DEFAULT_ICON_SIZE

class FrameController : EventConsumer, EventSender {
    companion object {
        private const val PLANE_DEPTH: Short = 10000
        private const val LAYER_DEPTH: Short = 1000
        private const val OBJ_DEPTH: Short = 100
        private const val MOB_DEPTH: Short = 10
    }

    private val cache: MutableList<FrameMesh> = mutableListOf()
    private val framedTiles: MutableList<FramedTile> = mutableListOf()

    private var currentIconSize: Int = DEFAULT_ICON_SIZE

    init {
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapZActiveChanged::class.java, ::handleSelectedMapZActiveChanged)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(Reaction.LayersFilterRefreshed::class.java, ::handleLayersFilterRefreshed)
        consumeEvent(TriggerFrameController.RefreshFrame::class.java, ::handleRefreshFrame)
    }

    fun postInit() {
        sendEvent(Provider.FrameControllerComposedFrame(cache))
        sendEvent(Provider.FrameControllerFramedTiles(framedTiles))
    }

    private fun updateFrameCache() {
        sendEvent(TriggerMapHolderController.FetchSelectedMap { map ->
            var filteredTypes: Set<String>? = null

            sendEvent(TriggerLayersFilterController.FetchFilteredLayers {
                filteredTypes = it
            })

            for (x in 1..map.maxX) {
                for (y in 1..map.maxY) {
                    readTileFrames(map, x, y)

                    for (tileItemId in map.getTileItemsId(x, y, map.zActive)) {
                        val tileItem = GlobalTileItemHolder.getById(tileItemId)

                        if (filteredTypes != null && filteredTypes!!.contains(tileItem.type)) {
                            continue
                        }

                        val sprite = GlobalDmiHolder.getIconSpriteOrPlaceholder(tileItem.icon, tileItem.iconState, tileItem.dir)
                        val x1 = (x - 1) * currentIconSize + tileItem.pixelX
                        val y1 = (y - 1) * currentIconSize + tileItem.pixelY
                        val x2 = x1 + sprite.iconWidth
                        val y2 = y1 + sprite.iconHeight
                        val colorR = tileItem.colorR
                        val colorG = tileItem.colorG
                        val colorB = tileItem.colorB
                        val colorA = tileItem.colorA
                        val depth = tileItem.plane * PLANE_DEPTH + tileItem.layer * LAYER_DEPTH

                        val specificDepth = when {
                            tileItem.isType(TYPE_OBJ) -> OBJ_DEPTH
                            tileItem.isType(TYPE_MOB) -> MOB_DEPTH
                            else -> 0
                        }

                        cache.add(FrameMesh(tileItemId, sprite, x1, y1, x2, y2, colorR, colorG, colorB, colorA, depth + specificDepth))
                    }
                }
            }

            cache.sortBy { it.depth }
        })
    }

    private fun readTileFrames(selectedMap: Dmm, x: Int, y: Int) {
        fun getArea(x: Int, y: Int): TileItem? {
            return if (x in 1..selectedMap.maxX && y in 1..selectedMap.maxY) {
                selectedMap.getTileItemsId(x, y, selectedMap.zActive).map { GlobalTileItemHolder.getById(it) }.find { it.isType(TYPE_AREA) }
            } else {
                null
            }
        }

        fun isFramedBorder(x: Int, y: Int, currentAreaType: String): Boolean = getArea(x, y)?.type != currentAreaType

        val currentAreaType = getArea(x, y)?.type ?: ""
        var dir = 0

        if (isFramedBorder(x - 1, y, currentAreaType)) dir = dir or WEST
        if (isFramedBorder(x + 1, y, currentAreaType)) dir = dir or EAST
        if (isFramedBorder(x, y - 1, currentAreaType)) dir = dir or SOUTH
        if (isFramedBorder(x, y + 1, currentAreaType)) dir = dir or NORTH

        if (dir != 0) {
            framedTiles.add(FramedTile(x, y, dir))
        }
    }

    private fun refreshFrame() {
        cache.clear()
        framedTiles.clear()
        updateFrameCache()
        sendEvent(Reaction.FrameRefreshed())
    }

    private fun handleSelectedMapChanged() {
        refreshFrame()
    }

    private fun handleSelectedMapZActiveChanged() {
        refreshFrame()
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        currentIconSize = event.body.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: DEFAULT_ICON_SIZE
        refreshFrame()
    }

    private fun handleEnvironmentReset() {
        cache.clear()
        framedTiles.clear()
    }

    private fun handleSelectedMapClosed() {
        cache.clear()
        framedTiles.clear()
    }

    private fun handleLayersFilterRefreshed() {
        refreshFrame()
    }

    private fun handleRefreshFrame() {
        refreshFrame()
    }
}
