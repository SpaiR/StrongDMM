package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.history.PlaceTileItemAction
import io.github.spair.strongdmm.logic.map.TileItem

fun MapGLRenderer.addItemToMap() {
    selectedMap?.let { map ->
        InstanceListView.selectedInstance?.let { selectedInstance ->
            val tileItem = TileItem.fromInstance(selectedInstance, xMouseMap, yMouseMap)
            map.placeTileItem(tileItem)
            History.addUndoAction(PlaceTileItemAction(map, tileItem))
            Frame.update(true)
        }
    }
}
