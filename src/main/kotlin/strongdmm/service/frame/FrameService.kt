package strongdmm.service.frame

import strongdmm.application.PostInitialize
import strongdmm.application.Service
import strongdmm.byond.*
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.*
import strongdmm.service.dmi.DmiCache
import strongdmm.util.DEFAULT_ICON_SIZE

class FrameService : Service, PostInitialize {
    companion object {
        private const val PLANE_DEPTH: Short = 10000
        private const val LAYER_DEPTH: Short = 1000
        private const val OBJ_DEPTH: Short = 100
        private const val MOB_DEPTH: Short = 10
    }

    private lateinit var providedDmiCache: DmiCache

    private val composedFrame: MutableList<FrameMesh> = mutableListOf()
    private val framedTiles: MutableList<FramedTile> = mutableListOf()

    private var currentIconSize: Int = DEFAULT_ICON_SIZE

    init {
        EventBus.sign(ProviderDmiService.DmiCache::class.java, ::handleProviderDmiCache)
        EventBus.sign(ReactionMapHolderService.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapZSelectedChanged::class.java, ::handleSelectedMapZSelectedChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapSizeChanged::class.java, ::handleSelectedMapSizeChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        EventBus.sign(ReactionEnvironmentService.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionLayersFilterService.LayersFilterRefreshed::class.java, ::handleLayersFilterRefreshed)
        EventBus.sign(TriggerFrameService.RefreshFrame::class.java, ::handleRefreshFrame)
    }

    override fun postInit() {
        EventBus.post(ProviderFrameService.ComposedFrame(composedFrame))
        EventBus.post(ProviderFrameService.FramedTiles(framedTiles))
    }

    private fun updateFrameCache() {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap { map ->
            var filteredTypes: Set<String>? = null

            EventBus.post(TriggerLayersFilterService.FetchFilteredLayers {
                filteredTypes = it
            })

            for (x in 1..map.maxX) {
                for (y in 1..map.maxY) {
                    readTileFrames(map, x, y)

                    for (tileItemId in map.getTileItemsId(x, y, map.zSelected)) {
                        val tileItem = GlobalTileItemHolder.getById(tileItemId)

                        if (filteredTypes != null && filteredTypes!!.contains(tileItem.type)) {
                            continue
                        }

                        val sprite = providedDmiCache.getIconSpriteOrPlaceholder(tileItem.icon, tileItem.iconState, tileItem.dir)
                        val x1 = (x - 1) * currentIconSize + tileItem.pixelX + tileItem.stepX
                        val y1 = (y - 1) * currentIconSize + tileItem.pixelY + tileItem.stepY
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

                        composedFrame.add(FrameMesh(tileItemId, sprite, x, y, x1, y1, x2, y2, colorR, colorG, colorB, colorA, depth + specificDepth))
                    }
                }
            }

            composedFrame.sortBy { it.depth }
        })
    }

    private fun readTileFrames(selectedMap: Dmm, x: Int, y: Int) {
        fun getArea(x: Int, y: Int): TileItem? {
            return if (x in 1..selectedMap.maxX && y in 1..selectedMap.maxY) {
                selectedMap.getTileItemsId(x, y, selectedMap.zSelected).map { GlobalTileItemHolder.getById(it) }.find { it.isType(TYPE_AREA) }
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
        composedFrame.clear()
        framedTiles.clear()
        updateFrameCache()
        EventBus.post(ReactionCanvasService.FrameRefreshed.SIGNAL)
    }

    private fun handleProviderDmiCache(event: Event<DmiCache, Unit>) {
        providedDmiCache = event.body
    }

    private fun handleSelectedMapChanged() {
        refreshFrame()
    }

    private fun handleSelectedMapZSelectedChanged() {
        refreshFrame()
    }

    private fun handleSelectedMapSizeChanged() {
        refreshFrame()
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        currentIconSize = event.body.getWorldIconSize()
        refreshFrame()
    }

    private fun handleEnvironmentReset() {
        composedFrame.clear()
        framedTiles.clear()
    }

    private fun handleSelectedMapClosed() {
        composedFrame.clear()
        framedTiles.clear()
    }

    private fun handleLayersFilterRefreshed() {
        refreshFrame()
    }

    private fun handleRefreshFrame() {
        refreshFrame()
    }
}
