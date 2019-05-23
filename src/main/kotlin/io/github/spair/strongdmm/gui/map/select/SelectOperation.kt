package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.menubar.MenuBarView

object SelectOperation : TileSelect {

    private var tileSelect: TileSelect = AddTileSelect()

    override fun onStart(x: Int, y: Int) = tileSelect.onStart(x, y)
    override fun onAdd(x: Int, y: Int) = tileSelect.onAdd(x, y)
    override fun onStop() = tileSelect.onStop()
    override fun isEmpty() = tileSelect.isEmpty()
    override fun render(iconSize: Int) = tileSelect.render(iconSize)

    fun switchSelectType(selectType: SelectType) {
        when (selectType) {
            SelectType.ADD -> tileSelect = AddTileSelect()
            SelectType.FILL -> tileSelect = FillTileSelect()
            SelectType.PICK -> tileSelect = PickTileSelect()
        }
        Frame.update()
    }

    fun pickArea(x1: Int, y1: Int, x2: Int, y2: Int) {
        tileSelect = PickTileSelect().apply {
            selectArea(x1, y1, x2, y2)
        }
        MenuBarView.switchSelectType(SelectType.PICK)
    }
}
