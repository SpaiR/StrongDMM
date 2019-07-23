package io.github.spair.strongdmm.logic.map

import io.github.spair.dmm.io.DmmData
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.Workspace
import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.action.ChangeMapSizeAction
import io.github.spair.strongdmm.logic.map.save.SaveMap
import java.io.File

object MapManager {

    fun setMapSize(dmm: Dmm, newMaxX: Int, newMaxY: Int) {
        val reverseAction = ChangeMapSizeAction(dmm, dmm.getMaxX(), dmm.getMaxY(), dmm.changeMapSize(newMaxX, newMaxY))
        ActionController.addUndoAction(reverseAction)
    }

    fun saveNewMap(file: File, initX: Int, initY: Int): Dmm {
        val dmmData = DmmData().apply {
            keyLength = 1
            setDmmSize(0, 0)
            isTgm = Workspace.isTgmSaveMode()
        }
        val dmm = Dmm(file, dmmData, Environment.dme)

        // Those two methods will place initial structure into the file
        dmm.changeMapSize(initX, initY)
        SaveMap(dmm)

        return dmm
    }
}
