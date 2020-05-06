package strongdmm.event.type.service

import strongdmm.byond.dmm.MapArea
import strongdmm.event.Event
import java.io.File

abstract class TriggerScreenshotService {
    class TakeScreenshot(body: Pair<File, MapArea>) : Event<Pair<File, MapArea>, Unit>(body, null)
}
