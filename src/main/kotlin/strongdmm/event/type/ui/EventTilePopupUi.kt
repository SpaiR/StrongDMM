package strongdmm.event.type.ui

import strongdmm.byond.dmm.Tile
import strongdmm.event.Event

abstract class EventTilePopupUi {
    class Open(body: Tile) : Event<Tile, Unit>(body, null)
    class Close : Event<Unit, Unit>(Unit, null)
}
