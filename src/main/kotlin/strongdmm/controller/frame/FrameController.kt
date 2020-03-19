package strongdmm.controller.frame

import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_ICON_SIZE
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventFrameController
import strongdmm.event.type.controller.EventLayersFilterController
import strongdmm.event.type.controller.EventMapHolderController
import strongdmm.util.DEFAULT_ICON_SIZE

class FrameController : EventConsumer, EventSender {
    companion object {
        private const val PLANE_DEPTH: Short = 10000
        private const val LAYER_DEPTH: Short = 1000
        private const val OBJ_DEPTH: Short = 100
        private const val MOB_DEPTH: Short = 10
    }

    private val cache: MutableList<FrameMesh> = mutableListOf()
    private var openedMapId: Int = Dmm.MAP_ID_NONE

    private var currentIconSize: Int = DEFAULT_ICON_SIZE

    init {
        consumeEvent(EventGlobal.OpenedMapChanged::class.java, ::handleOpenedMapChanged)
        consumeEvent(EventGlobal.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(EventFrameController.Refresh::class.java, ::handleRefresh)
    }

    fun postInit() {
        sendEvent(EventGlobalProvider.ComposedFrame(cache))
    }

    private fun handleOpenedMapChanged(event: Event<Dmm, Unit>) {
        openedMapId = event.body.id
        cache.clear()
        updateFrameCache()
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        currentIconSize = event.body.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: DEFAULT_ICON_SIZE
        updateFrameCache()
    }

    private fun handleEnvironmentReset() {
        openedMapId = Dmm.MAP_ID_NONE
        cache.clear()
    }

    private fun handleOpenedMapClosed(event: Event<Dmm, Unit>) {
        if (openedMapId == event.body.id) {
            openedMapId = Dmm.MAP_ID_NONE
            cache.clear()
        }
    }

    private fun handleRefresh() {
        cache.clear()
        updateFrameCache()
        sendEvent(EventGlobal.FrameRefreshed())
    }

    private fun updateFrameCache() {
        sendEvent(EventMapHolderController.FetchSelected { map ->
            var filteredTypes: Set<String>? = null

            sendEvent(EventLayersFilterController.Fetch {
                filteredTypes = it
            })

            for (x in 1..map.maxX) {
                for (y in 1..map.maxY) {
                    for (tileItemId in map.getTileItemsId(x, y)) {
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
}
