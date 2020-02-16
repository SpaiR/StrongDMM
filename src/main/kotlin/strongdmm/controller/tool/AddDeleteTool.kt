package strongdmm.controller.tool

import imgui.ImGui
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem

class AddDeleteTool : Tool() {
    override var isActive: Boolean
        get() = currentTool.isActive
        set(value) {
            currentTool.isActive = value
        }

    private val add: Tool = AddTool()
    private val delete: Tool = DeleteTool()

    private var currentTool: Tool = add

    override fun onStart(mapPos: MapPos) {
        currentTool = if (ImGui.getIO().keyShift) delete else add
        currentTool.onStart(mapPos)
    }

    override fun onStop() {
        currentTool.onStop()
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        currentTool.onMapPosChanged(mapPos)
    }

    override fun onTileItemSwitch(tileItem: TileItem) {
        add.onTileItemSwitch(tileItem)
        delete.onTileItemSwitch(tileItem)
    }

    override fun onMapSwitch(map: Dmm) {
        add.onMapSwitch(map)
        delete.onMapSwitch(map)
    }
}
