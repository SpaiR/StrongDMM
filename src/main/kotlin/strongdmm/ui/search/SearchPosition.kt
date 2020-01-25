package strongdmm.ui.search

import strongdmm.byond.dmm.MapPos

data class SearchPosition(
    val idx: Int,
    val type: String,
    val pos: MapPos
)
