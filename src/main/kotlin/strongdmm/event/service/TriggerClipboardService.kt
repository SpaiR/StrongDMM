package strongdmm.event.service

import strongdmm.event.Event

abstract class TriggerClipboardService {
    class Cut : Event<Unit, Unit>(Unit, null)
    class Copy : Event<Unit, Unit>(Unit, null)
    class Paste : Event<Unit, Unit>(Unit, null)
}
