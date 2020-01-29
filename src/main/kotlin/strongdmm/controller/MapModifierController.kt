package strongdmm.controller

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

class MapModifierController : EventConsumer, EventSender {
    init {
        consumeEvent(Event.MapModifierController.ReplaceTypeInPositions::class.java, ::handleReplaceTypeInPositions)
        consumeEvent(Event.MapModifierController.ReplaceIdInPositions::class.java, ::handleReplaceIdInPositions)
        consumeEvent(Event.MapModifierController.DeleteTypeInPositions::class.java, ::handleDeleteTypeInPositions)
        consumeEvent(Event.MapModifierController.DeleteIdInPositions::class.java, ::handleDeleteIdInPositions)
    }

    private fun handleReplaceTypeInPositions(event: Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>) {
        sendEvent(Event.MapHolderController.FetchSelected { dmm ->
            if (dmm != null) {
                val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first, null)
                val replaceActions = mutableListOf<Undoable>()

                event.body.second.forEach { (tileItem, pos) ->
                    val tile = dmm.getTile(pos.x, pos.y)
                    replaceActions.add(ReplaceTileAction(tile) {
                        tile.replaceTileItem(tileItem.type, replaceWithTileItem)
                    })
                }

                sendEvent(Event.ActionController.AddAction(MultiAction(replaceActions)))
                sendEvent(Event.Global.RefreshFrame())
            }
        })
    }

    private fun handleReplaceIdInPositions(event: Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>) {
        sendEvent(Event.MapHolderController.FetchSelected { dmm ->
            if (dmm != null) {
                val replaceWithTileItem = GlobalTileItemHolder.getOrCreate(event.body.first, null)
                val replaceActions = mutableListOf<Undoable>()

                event.body.second.forEach { (tileItem, pos) ->
                    val tile = dmm.getTile(pos.x, pos.y)
                    replaceActions.add(ReplaceTileAction(tile) {
                        tile.replaceTileItem(tileItem.id, replaceWithTileItem)
                    })
                }

                sendEvent(Event.ActionController.AddAction(MultiAction(replaceActions)))
                sendEvent(Event.Global.RefreshFrame())
            }
        })
    }

    private fun handleDeleteTypeInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        sendEvent(Event.MapHolderController.FetchSelected { dmm ->
            if (dmm != null) {
                val deleteActions = mutableListOf<Undoable>()

                event.body.forEach { (tileItem, pos) ->
                    val tile = dmm.getTile(pos.x, pos.y)
                    deleteActions.add(ReplaceTileAction(tile) {
                        tile.deleteTileItem(tileItem.type)
                    })
                }

                sendEvent(Event.ActionController.AddAction(MultiAction(deleteActions)))
                sendEvent(Event.Global.RefreshFrame())
            }
        })
    }

    private fun handleDeleteIdInPositions(event: Event<List<Pair<TileItem, MapPos>>, Unit>) {
        sendEvent(Event.MapHolderController.FetchSelected { dmm ->
            if (dmm != null) {
                val deleteActions = mutableListOf<Undoable>()

                event.body.forEach { (tileItem, pos) ->
                    val tile = dmm.getTile(pos.x, pos.y)
                    deleteActions.add(ReplaceTileAction(tile) {
                        tile.deleteTileItem(tileItem.id)
                    })
                }

                sendEvent(Event.ActionController.AddAction(MultiAction(deleteActions)))
                sendEvent(Event.Global.RefreshFrame())
            }
        })
    }
}
