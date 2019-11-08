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
import strongdmm.event.Message
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
    private var selectedMapId: Int = -1

    private var currentIconSize: Int = DEFAULT_ICON_SIZE

    init {
        consumeEvent(Event.GLOBAL_SWITCH_MAP, ::handleSwitchMap)
        consumeEvent(Event.GLOBAL_SWITCH_ENVIRONMENT, ::handleSwitchEnvironment)
        consumeEvent(Event.GLOBAL_RESET_ENVIRONMENT, ::handleResetEnvironment)
        consumeEvent(Event.GLOBAL_CLOSE_MAP, ::handleCloseMap)
        consumeEvent(Event.FRAME_COMPOSE, ::handleCompose)
    }

    private fun handleSwitchMap(msg: Message<Dmm, Unit>) {
        selectedMapId = msg.body.id
        cache.clear()
    }

    private fun handleSwitchEnvironment(msg: Message<Dme, Unit>) {
        currentIconSize = msg.body.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: DEFAULT_ICON_SIZE
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        selectedMapId = -1
        cache.clear()
    }

    private fun handleCloseMap(msg: Message<Dmm, Unit>) {
        if (selectedMapId == msg.body.id) {
            selectedMapId = -1
            cache.clear()
        }
    }

    private fun handleCompose(msg: Message<Unit, List<FrameMesh>>) {
        if (cache.isNotEmpty()) {
            msg.reply(cache)
            return
        }

        sendEvent<Dmm?>(Event.MAP_FETCH_SELECTED) { map ->
            if (map == null) {
                msg.reply(emptyList())
                return@sendEvent
            }

            for (x in 1..map.getMaxX()) {
                for (y in 1..map.getMaxY()) {
                    map.getTileItems(x, y).forEach { tileItemId ->
                        val tileItem = GlobalTileItemHolder.getById(tileItemId)
                        val sprite = GlobalDmiHolder.getSprite(tileItem.icon, tileItem.iconState, tileItem.dir)
                        val x1 = (x - 1) * currentIconSize + tileItem.pixelX
                        val y1 = (y - 1) * currentIconSize + tileItem.pixelY
                        val x2 = x1 + sprite.iconWidth
                        val y2 = y1 + sprite.iconHeight
                        val color = colorExtractor.extract(tileItem)
                        val depth = tileItem.plane * PLANE_DEPTH + tileItem.layer * LAYER_DEPTH

                        val specificDepth = when {
                            tileItem.type.startsWith(TYPE_OBJ) -> OBJ_DEPTH
                            tileItem.type.startsWith(TYPE_MOB) -> MOB_DEPTH
                            else -> 0
                        }

                        cache.add(FrameMesh(sprite, x1, y1, x2, y2, color, depth + specificDepth))
                    }
                }
            }

            cache.sortBy { it.depth }
            msg.reply(cache)
        }
    }
}
