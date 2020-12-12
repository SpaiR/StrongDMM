package strongdmm.event.service

import strongdmm.event.Event

abstract class TriggerSettingsService {
    class SaveSettings : Event<Unit, Unit>(Unit, null)
}
