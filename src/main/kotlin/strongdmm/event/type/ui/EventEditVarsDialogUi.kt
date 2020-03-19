package strongdmm.event.type.ui

import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.TileItemIdx

abstract class EventEditVarsDialogUi {
    class OpenWithTile(body: Pair<Tile, TileItemIdx>) : Event<Pair<Tile, TileItemIdx>, Unit>(body, null)
    class OpenWithTileItem(body: TileItem) : Event<TileItem, Unit>(body, null)
}
