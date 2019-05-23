package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.select.SelectOperation

class PickAreaAction(
    private val x11: Int, private val y11: Int, private val x12: Int, private val y12: Int,
    private val x21: Int, private val y21: Int, private val x22: Int, private val y22: Int
) : Undoable {
    override fun doAction(): Undoable {
        SelectOperation.pickArea(x11, y11, x12, y12)
        Frame.update()
        return PickAreaAction(x21, y21, x22, y22, x11, y11, x12, y12)
    }
}
