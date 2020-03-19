package strongdmm.event.type.controller

import strongdmm.event.Event

abstract class EventClipboardController {
    class Copy : Event<Unit, Unit>(Unit, null)
    class Paste : Event<Unit, Unit>(Unit, null)
}
