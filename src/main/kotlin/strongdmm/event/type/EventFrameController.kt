package strongdmm.event.type

import strongdmm.event.Event

abstract class EventFrameController {
    class Refresh : Event<Unit, Unit>(Unit, null)
}
