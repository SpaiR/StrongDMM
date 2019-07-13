package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.action.ChangeMapSizeAction

object MapManager {

    fun setMapSize(dmm: Dmm, newMaxX: Int, newMaxY: Int) {
        val reverseAction = ChangeMapSizeAction(dmm, dmm.getMaxX(), dmm.getMaxY(), dmm.changeMapSize(newMaxX, newMaxY))
        ActionController.addUndoAction(reverseAction)
    }
}
