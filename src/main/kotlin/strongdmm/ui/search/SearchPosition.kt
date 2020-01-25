package strongdmm.ui.search

import strongdmm.byond.dmm.MapPos

data class SearchPosition(
    val idx: Int,
    val tileItemType: String,
    val pos: MapPos
)
