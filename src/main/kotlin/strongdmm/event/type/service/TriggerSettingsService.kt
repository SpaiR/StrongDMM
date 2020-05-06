package strongdmm.event.type.service

import strongdmm.event.Event

abstract class TriggerSettingsService {
    class SaveSettings : Event<Unit, Unit>(Unit, null)
}
