package strongdmm.controller.map

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.MultiAction
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.action.undoable.Undoable
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.TileItemType
import strongdmm.event.type.controller.EventActionController
import strongdmm.event.type.controller.EventFrameController
import strongdmm.event.type.controller.EventMapHolderController
import strongdmm.event.type.controller.EventMapModifierController

class MapModifierController : EventConsumer, EventSender {
    init {
        consumeEvent(EventMapModifierController.ReplaceTypeInPositions::class.java, ::handleReplaceTypeInPositions)
        consumeEvent(EventMapModifierController.ReplaceIdInPositions::class.java, ::handleReplaceIdInPositions)
        consumeEvent(EventMapModifierController.DeleteTypeInPositions::class.java, ::handleDeleteTypeInPositions)
        consumeEvent(EventMapModifierController.DeleteIdInPositions::class.java, ::handleDeleteIdInPositions)
    }

    private fun handleReplaceTypeInPositions(event: Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelected { dmm ->
            val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first)
            val replaceActions = mutableListOf<Undoable>()

            event.body.second.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                replaceActions.add(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem.type, replaceWithTileItem)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(replaceActions)))
            sendEvent(EventFrameController.Refresh())
        })
    }

    private fun handleReplaceIdInPositions(event: Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelected { dmm ->
            val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first)
            val replaceActions = mutableListOf<Undoable>()

            event.body.second.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                replaceActions.add(ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItem.id, replaceWithTileItem)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(replaceActions)))
            sendEvent(EventFrameController.Refresh())
        })
    }

    private fun handleDeleteTypeInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelected { dmm ->
            val deleteActions = mutableListOf<Undoable>()

            event.body.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                deleteActions.add(ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem.type)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(deleteActions)))
            sendEvent(EventFrameController.Refresh())
        })
    }

    private fun handleDeleteIdInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        sendEvent(EventMapHolderController.FetchSelected { dmm ->
            val deleteActions = mutableListOf<Undoable>()

            event.body.forEach { (tileItem, pos) ->
                val tile = dmm.getTile(pos.x, pos.y)
                deleteActions.add(ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItem.id)
                })
            }

            sendEvent(EventActionController.AddAction(MultiAction(deleteActions)))
            sendEvent(EventFrameController.Refresh())
        })
    }
}
