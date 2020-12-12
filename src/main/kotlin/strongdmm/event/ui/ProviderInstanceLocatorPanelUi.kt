package strongdmm.event.ui

import imgui.type.ImBoolean
import strongdmm.event.Event

abstract class ProviderInstanceLocatorPanelUi {
    class DoInstanceLocatorOpen(body: ImBoolean) : Event<ImBoolean, Unit>(body, null)
}
