package strongdmm.event.type.service

import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event

abstract class ProviderRecentFilesService {
    class RecentEnvironmentsWithMaps(body: Map<String, List<MapPath>>) : Event<Map<String, List<MapPath>>, Unit>(body, null)
    class RecentEnvironments(body: List<String>) : Event<List<String>, Unit>(body, null)
    class RecentMaps(body: List<MapPath>) : Event<List<MapPath>, Unit>(body, null)
}
