package strongdmm.event.type.controller

import strongdmm.event.Event

abstract class TriggerClipboardController {
    class Cut : Event<Unit, Unit>(Unit, null)
    class Copy : Event<Unit, Unit>(Unit, null)
    class Paste : Event<Unit, Unit>(Unit, null)
}
