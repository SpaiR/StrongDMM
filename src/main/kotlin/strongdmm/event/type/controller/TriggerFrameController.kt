package strongdmm.event.type.controller

import strongdmm.event.Event

abstract class TriggerFrameController {
    class RefreshFrame : Event<Unit, Unit>(Unit, null)
}
