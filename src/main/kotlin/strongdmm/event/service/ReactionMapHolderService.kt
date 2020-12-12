package strongdmm.event.service

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapSize
import strongdmm.event.Event

abstract class ReactionMapHolderService {
    class SelectedMapChanged(body: Dmm) : Event<Dmm, Unit>(body, null)

    class SelectedMapClosed private constructor() : Event<Unit, Unit>(Unit, null) {
        companion object {
            val SIGNAL = SelectedMapClosed()
        }
    }

    class OpenedMapClosed(body: Dmm) : Event<Dmm, Unit>(body, null)
    class SelectedMapZSelectedChanged(body: Int) : Event<Int, Unit>(body, null)
    class SelectedMapSizeChanged(body: MapSize) : Event<MapSize, Unit>(body, null)
}
