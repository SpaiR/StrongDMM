package strongdmm.controller.tool.fill

import imgui.ImGui
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.tool.Tool

class FillComplexTool : Tool() {
    override var isActive: Boolean
        get() = currentTool.isActive
        set(value) {
            currentTool.isActive = value
        }

    private val add: Tool = FillAddTool()
    private val delete: Tool = FillDeleteTool()

    private var currentTool: Tool = add

    override fun onStart(mapPos: MapPos) {
        currentTool = if (ImGui.getIO().keyCtrl) delete else add
        currentTool.onStart(mapPos)
    }

    override fun onStop() {
        currentTool.onStop()
    }

    override fun onMapPosChanged(mapPos: MapPos) {
        currentTool.onMapPosChanged(mapPos)
    }

    override fun onTileItemSwitch(tileItem: TileItem?) {
        add.onTileItemSwitch(tileItem)
        delete.onTileItemSwitch(tileItem)
    }

    override fun onMapSwitch(map: Dmm?) {
        add.onMapSwitch(map)
        delete.onMapSwitch(map)
    }
}
