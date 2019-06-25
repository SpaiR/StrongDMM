package io.github.spair.strongdmm.logic.action

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Dmm

class PlaceTileItemAction(
    private val map: Dmm,
    private val x: Int,
    private val y: Int,
    private val tileItemID: Int
) : Undoable {
    override fun doAction(): Undoable {
        val reverseAction = map.placeTileItemWithUndoableByID(x, y, tileItemID)
        Frame.update(true)
        return reverseAction
    }
}
