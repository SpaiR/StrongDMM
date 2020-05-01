package strongdmm.ui.panel.search_result.model

import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem

data class SearchPosition(
    val tileItem: TileItem,
    val pos: MapPos
)
