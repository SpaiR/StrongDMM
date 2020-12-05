package strongdmm.event.type.service

import imgui.type.ImBoolean
import strongdmm.event.Event

abstract class ProviderCanvasService {
    class DoFrameAreas(body: ImBoolean) : Event<ImBoolean, Unit>(body, null)
    class DoSynchronizeMapsView(body: ImBoolean) : Event<ImBoolean, Unit>(body, null)
}
