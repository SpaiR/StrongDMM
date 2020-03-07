package strongdmm.controller.tool

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem

abstract class Tool {
    open var isActive: Boolean = false

    abstract fun onStart(mapPos: MapPos)
    abstract fun onStop()
    abstract fun onMapPosChanged(mapPos: MapPos)
    abstract fun onTileItemSwitch(tileItem: TileItem?)
    abstract fun onMapSwitch(map: Dmm?)
    abstract fun reset()
    abstract fun destroy()
}
