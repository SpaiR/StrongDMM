package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.logic.history.DeleteTileItemAction
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.history.PlaceTileItemAction
import io.github.spair.strongdmm.logic.map.TileItem

fun MapGLRenderer.placeItemOnMap() {
    selectedMap?.let { map ->
        InstanceListView.selectedInstance?.let { selectedInstance ->
            val tileItem = TileItem.fromInstance(selectedInstance, xMouseMap, yMouseMap)
            val removedItem = map.placeTileItem(tileItem)

            // if removedItem is not null then it means that we placed replaceable type (turf or area)
            // to undo this action we need to place removed item back
            if (removedItem != null) {
                History.addUndoAction(PlaceTileItemAction(map, removedItem))
            } else {
                History.addUndoAction(DeleteTileItemAction(map, tileItem))
            }

            Frame.update(true)
        }
    }
}
