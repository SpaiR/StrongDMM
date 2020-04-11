package strongdmm.event.type.controller

import strongdmm.event.Event

abstract class TriggerPreferencesController {
    class SavePreferences : Event<Unit, Unit>(Unit, null)
}
