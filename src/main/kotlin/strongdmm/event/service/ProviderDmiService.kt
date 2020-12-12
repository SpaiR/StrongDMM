package strongdmm.event.service

import strongdmm.event.Event
import strongdmm.service.dmi.DmiCache as DCache

abstract class ProviderDmiService {
    class DmiCache(body: DCache) : Event<DCache, Unit>(body, null)
}
