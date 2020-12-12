package strongdmm.event.ui

import strongdmm.event.Event

abstract class TriggerNotificationPanelUi {
    class Notify(body: String) : Event<String, Unit>(body, null)
}
