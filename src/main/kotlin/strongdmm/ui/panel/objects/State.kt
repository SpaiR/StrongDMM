package strongdmm.ui.panel.objects

import imgui.type.ImInt
import strongdmm.byond.dmm.TileItem
import strongdmm.service.dmi.DmiCache

class State {
    lateinit var providedDmiCache: DmiCache

    val columnsCount: ImInt = ImInt(1)

    var scrolledToItem: Boolean = false
    var selectedTileItemType: String = ""
    var tileItems: List<TileItem>? = null
    var selectedTileItemId: Long = 0L
}
