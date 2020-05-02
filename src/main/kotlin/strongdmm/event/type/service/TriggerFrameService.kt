package strongdmm.event.type.service

import strongdmm.event.Event

abstract class TriggerFrameService {
    class RefreshFrame : Event<Unit, Unit>(Unit, null)
}
