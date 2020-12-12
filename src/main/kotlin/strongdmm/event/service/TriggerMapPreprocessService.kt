package strongdmm.event.service

import strongdmm.byond.dmm.parser.DmmData
import strongdmm.event.Event

abstract class TriggerMapPreprocessService {
    class Preprocess(body: DmmData, callback: (Unit) -> Unit) : Event<DmmData, Unit>(body, callback)
}
