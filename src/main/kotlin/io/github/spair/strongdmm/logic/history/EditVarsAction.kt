package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.TileItem

class EditVarsAction(private val tileItem: TileItem) : Undoable {

    private val initialVars = tileItem.customVars.toMap()

    override fun doAction(): Undoable {
        val reverseAction = EditVarsAction(tileItem)
        tileItem.customVars.apply { clear() }.putAll(initialVars)
        tileItem.updateFields()
        Frame.update(true)
        return reverseAction
    }
}
