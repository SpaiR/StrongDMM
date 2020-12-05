package strongdmm.event.type.service

import strongdmm.event.Event
import strongdmm.service.settings.Settings as STS

abstract class ProviderSettingsService {
    class Settings(body: STS) : Event<STS, Unit>(body, null)
}
