package strongdmm.event.type

import gnu.trove.map.hash.TObjectIntHashMap
import imgui.ImBool
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPath
import strongdmm.controller.frame.FrameMesh
import strongdmm.controller.frame.FramedTile
import strongdmm.controller.preferences.Preferences
import strongdmm.event.AbsoluteFilePath
import strongdmm.event.Event
import strongdmm.event.VisibleFilePath

abstract class Provider {
    class InstanceLocatorPanelUiOpen(body: ImBool) : Event<ImBool, Unit>(body, null)
    class ActionControllerActionBalanceStorage(body: TObjectIntHashMap<Dmm>) : Event<TObjectIntHashMap<Dmm>, Unit>(body, null)
    class CanvasControllerFrameAreas(body: ImBool) : Event<ImBool, Unit>(body, null)
    class PreferencesControllerPreferences(body: Preferences) : Event<Preferences, Unit>(body, null)

    class MapHolderControllerOpenedMaps(body: Set<Dmm>) : Event<Set<Dmm>, Unit>(body, null)
    class MapHolderControllerAvailableMaps(body: Set<Pair<AbsoluteFilePath, VisibleFilePath>>) :
        Event<Set<Pair<AbsoluteFilePath, VisibleFilePath>>, Unit>(body, null)

    class FrameControllerComposedFrame(body: List<FrameMesh>) : Event<List<FrameMesh>, Unit>(body, null)
    class FrameControllerFramedTiles(body: List<FramedTile>) : Event<List<FramedTile>, Unit>(body, null)

    class RecentFilesControllerRecentEnvironments(body: List<String>) : Event<List<String>, Unit>(body, null)
    class RecentFilesControllerRecentMaps(body: List<MapPath>) : Event<List<MapPath>, Unit>(body, null)
}
