package strongdmm.event.type

import gnu.trove.map.hash.TObjectIntHashMap
import imgui.type.ImBoolean
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event
import strongdmm.service.frame.FrameMesh
import strongdmm.service.frame.FramedTile
import strongdmm.service.preferences.Preferences
import strongdmm.service.settings.Settings
import strongdmm.util.imgui.markdown.ImMarkdown

abstract class Provider {
    class InstanceLocatorPanelUiOpen(body: ImBoolean) : Event<ImBoolean, Unit>(body, null)
    class ActionServiceActionBalanceStorage(body: TObjectIntHashMap<Dmm>) : Event<TObjectIntHashMap<Dmm>, Unit>(body, null)
    class CanvasServiceFrameAreas(body: ImBoolean) : Event<ImBoolean, Unit>(body, null)
    class CanvasServiceSynchronizeMapsView(body: ImBoolean) : Event<ImBoolean, Unit>(body, null)
    class PreferencesServicePreferences(body: Preferences) : Event<Preferences, Unit>(body, null)
    class SettingsServiceSettings(body: Settings) : Event<Settings, Unit>(body, null)
    class ChangelogServiceChangelogMarkdown(changelogMarkdown: ImMarkdown) : Event<ImMarkdown, Unit>(changelogMarkdown, null)

    class MapHolderServiceOpenedMaps(body: Set<Dmm>) : Event<Set<Dmm>, Unit>(body, null)
    class MapHolderServiceAvailableMaps(body: Set<MapPath>) : Event<Set<MapPath>, Unit>(body, null)

    class FrameServiceComposedFrame(body: List<FrameMesh>) : Event<List<FrameMesh>, Unit>(body, null)
    class FrameServiceFramedTiles(body: List<FramedTile>) : Event<List<FramedTile>, Unit>(body, null)

    class RecentFilesServiceRecentEnvironmentsWithMaps(body: Map<String, List<MapPath>>) : Event<Map<String, List<MapPath>>, Unit>(body, null)
    class RecentFilesServiceRecentEnvironments(body: List<String>) : Event<List<String>, Unit>(body, null)
    class RecentFilesServiceRecentMaps(body: List<MapPath>) : Event<List<MapPath>, Unit>(body, null)
}
