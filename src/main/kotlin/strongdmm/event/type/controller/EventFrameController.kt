package strongdmm.event.type.controller

import strongdmm.event.Event

abstract class EventFrameController {
    class RefreshFrame : Event<Unit, Unit>(Unit, null)
}
