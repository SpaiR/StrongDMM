package strongdmm.event.service

import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event

abstract class ReactionCanvasService {
    class FrameRefreshed private constructor() : Event<Unit, Unit>(Unit, null) {
        companion object {
            val SIGNAL = FrameRefreshed()
        }
    }

    class MapMousePosChanged(body: MapPos) : Event<MapPos, Unit>(body, null)

    class MapMouseDragStarted private constructor() : Event<Unit, Unit>(Unit, null) {
        companion object {
            val SIGNAL = MapMouseDragStarted()
        }
    }

    class MapMouseDragStopped private constructor() : Event<Unit, Unit>(Unit, null) {
        companion object {
            val SIGNAL = MapMouseDragStopped()
        }
    }
}
