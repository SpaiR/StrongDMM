package strongdmm.ui.panel.objects

import imgui.ImInt
import strongdmm.byond.dmm.TileItem

class State {
    val columnsCount: ImInt = ImInt(1)

    var scrolledToItem: Boolean = false
    var selectedTileItemType: String = ""
    var tileItems: List<TileItem>? = null
    var selectedTileItemId: Long = 0L
}
