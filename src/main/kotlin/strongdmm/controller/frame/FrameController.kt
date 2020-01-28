package strongdmm.controller.frame

import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_WORLD
import strongdmm.byond.VAR_ICON_SIZE
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.ColorExtractor
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.DEFAULT_ICON_SIZE

class FrameController : EventConsumer, EventSender {
    companion object {
        private const val PLANE_DEPTH: Short = 10000
        private const val LAYER_DEPTH: Short = 1000
        private const val OBJ_DEPTH: Short = 100
        private const val MOB_DEPTH: Short = 10
    }

    private val colorExtractor = ColorExtractor()
    private val cache: MutableList<FrameMesh> = mutableListOf()
    private var selectedMapId: Int = Dmm.MAP_ID_NONE

    private var currentIconSize: Int = DEFAULT_ICON_SIZE

    init {
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.SwitchEnvironment::class.java, ::handleSwitchEnvironment)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
        consumeEvent(Event.Global.RefreshFrame::class.java, ::handleRefreshFrame)
        consumeEvent(Event.FrameController.Compose::class.java, ::handleCompose)
    }

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        selectedMapId = event.body.id
        cache.clear()
    }

    private fun handleSwitchEnvironment(event: Event<Dme, Unit>) {
        currentIconSize = event.body.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: DEFAULT_ICON_SIZE
    }

    private fun handleResetEnvironment() {
        selectedMapId = Dmm.MAP_ID_NONE
        cache.clear()
    }

    private fun handleCloseMap(event: Event<Dmm, Unit>) {
        if (selectedMapId == event.body.id) {
            selectedMapId = Dmm.MAP_ID_NONE
            cache.clear()
        }
    }

    private fun handleRefreshFrame() {
        cache.clear()
    }

    private fun handleCompose(event: Event<Unit, List<FrameMesh>>) {
        if (cache.isNotEmpty()) {
            event.reply(cache)
            return
        }

        sendEvent(Event.MapController.FetchSelected { map ->
            if (map == null) {
                event.reply(emptyList())
                return@FetchSelected
            }

            for (x in 1..map.maxX) {
                for (y in 1..map.maxY) {
                    map.getTileItemsId(x, y).forEach { tileItemId ->
                        val tileItem = GlobalTileItemHolder.getById(tileItemId)
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
                            tileItem.type.startsWith(TYPE_OBJ) -> OBJ_DEPTH
                            tileItem.type.startsWith(TYPE_MOB) -> MOB_DEPTH
                            else -> 0
                        }

                        cache.add(FrameMesh(sprite, x1, y1, x2, y2, colorR, colorG, colorB, colorA, depth + specificDepth))
                    }
                }
            }

            cache.sortBy { it.depth }
            event.reply(cache)
        })
    }
}
