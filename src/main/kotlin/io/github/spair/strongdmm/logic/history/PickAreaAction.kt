package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.logic.map.CoordArea

class PickAreaAction(private val initialArea: CoordArea, private val moveToArea: CoordArea) : Undoable {

    constructor(x11: Int, y11: Int, x12: Int, y12: Int, x21: Int, y21: Int, x22: Int, y22: Int)
            : this(CoordArea(x11, y11, x12, y12), CoordArea(x21, y21, x22, y22))

    override fun doAction(): Undoable {
        SelectOperation.pickArea(initialArea)
        Frame.update()
        return PickAreaAction(moveToArea, initialArea)
    }
}
