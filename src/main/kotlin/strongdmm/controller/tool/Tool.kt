package strongdmm.controller.tool

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.util.OUT_OF_BOUNDS

abstract class Tool {
    open var isActive: Boolean = false

    abstract fun onStart(mapPos: MapPos)
    abstract fun onStop()
    abstract fun onMapPosChanged(mapPos: MapPos)
    abstract fun onTileItemSwitch(tileItem: TileItem?)
    open fun getActiveArea(): MapArea = MapArea(OUT_OF_BOUNDS, OUT_OF_BOUNDS, OUT_OF_BOUNDS, OUT_OF_BOUNDS)
    abstract fun reset()
    abstract fun destroy()
}
