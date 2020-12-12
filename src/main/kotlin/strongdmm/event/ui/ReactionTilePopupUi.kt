package strongdmm.event.ui

import strongdmm.event.Event

abstract class ReactionTilePopupUi {
    class TilePopupOpened private constructor() : Event<Unit, Unit>(Unit, null) {
        companion object {
            val SIGNAL = TilePopupOpened()
        }
    }

    class TilePopupClosed private constructor() : Event<Unit, Unit>(Unit, null) {
        companion object {
            val SIGNAL = TilePopupClosed()
        }
    }
}
