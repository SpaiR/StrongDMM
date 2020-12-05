package strongdmm.event.type.service

import strongdmm.event.Event
import strongdmm.service.preferences.Preferences as Prefs

abstract class ProviderPreferencesService {
    class Preferences(body: Prefs) : Event<Prefs, Unit>(body, null)
}
