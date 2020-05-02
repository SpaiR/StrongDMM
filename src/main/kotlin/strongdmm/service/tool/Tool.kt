package strongdmm.service.tool

import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem

abstract class Tool {
    open var isActive: Boolean = false

    abstract fun onStart(mapPos: MapPos)
    abstract fun onStop()
    abstract fun onMapPosChanged(mapPos: MapPos)
    abstract fun onTileItemSwitch(tileItem: TileItem?)
    open fun getSelectedArea(): MapArea = MapArea.OUT_OF_BOUNDS_AREA
    abstract fun reset()
    abstract fun destroy()
}
