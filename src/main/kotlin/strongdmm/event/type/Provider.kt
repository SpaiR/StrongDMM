package strongdmm.event.type

import gnu.trove.map.hash.TObjectIntHashMap
import imgui.ImBool
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event
import strongdmm.service.frame.FrameMesh
import strongdmm.service.frame.FramedTile
import strongdmm.service.preferences.Preferences

abstract class Provider {
    class InstanceLocatorPanelUiOpen(body: ImBool) : Event<ImBool, Unit>(body, null)
    class ActionControllerActionBalanceStorage(body: TObjectIntHashMap<Dmm>) : Event<TObjectIntHashMap<Dmm>, Unit>(body, null)
    class CanvasControllerFrameAreas(body: ImBool) : Event<ImBool, Unit>(body, null)
    class PreferencesControllerPreferences(body: Preferences) : Event<Preferences, Unit>(body, null)
    class ChangelogControllerChangelogText(changelogText: String) : Event<String, Unit>(changelogText, null)

    class MapHolderControllerOpenedMaps(body: Set<Dmm>) : Event<Set<Dmm>, Unit>(body, null)
    class MapHolderControllerAvailableMaps(body: Set<MapPath>) : Event<Set<MapPath>, Unit>(body, null)

    class FrameControllerComposedFrame(body: List<FrameMesh>) : Event<List<FrameMesh>, Unit>(body, null)
    class FrameControllerFramedTiles(body: List<FramedTile>) : Event<List<FramedTile>, Unit>(body, null)

    class RecentFilesControllerRecentEnvironments(body: List<String>) : Event<List<String>, Unit>(body, null)
    class RecentFilesControllerRecentMaps(body: List<MapPath>) : Event<List<MapPath>, Unit>(body, null)
}
