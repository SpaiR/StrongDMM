package strongdmm.event.type.controller

import strongdmm.event.Event

abstract class EventPreferencesController {
    class SavePreferences : Event<Unit, Unit>(Unit, null)
}
