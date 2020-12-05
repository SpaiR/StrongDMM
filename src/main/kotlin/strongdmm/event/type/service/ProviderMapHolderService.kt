package strongdmm.event.type.service

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event

abstract class ProviderMapHolderService {
    class OpenedMaps(body: Set<Dmm>) : Event<Set<Dmm>, Unit>(body, null)
    class AvailableMaps(body: Set<MapPath>) : Event<Set<MapPath>, Unit>(body, null)
}
