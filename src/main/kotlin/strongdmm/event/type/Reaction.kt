package strongdmm.event.type

import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.MapSize
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.service.action.ActionStatus
import strongdmm.service.shortcut.Shortcut
import strongdmm.service.tool.ToolType
import java.io.File

abstract class Reaction {
    class ApplicationBlockChanged(applicationBlockStatus: Boolean) : Event<Boolean, Unit>(applicationBlockStatus, null)

    class EnvironmentReset : Event<Unit, Unit>(Unit, null)
    class EnvironmentLoadStarted(body: File) : Event<File, Unit>(body, null)
    class EnvironmentLoadStopped(environmentLoadedStatus: Boolean) : Event<Boolean, Unit>(environmentLoadedStatus, null)
    class EnvironmentChanged(body: Dme) : Event<Dme, Unit>(body, null)

    class SelectedMapChanged(body: Dmm) : Event<Dmm, Unit>(body, null)
    class SelectedMapClosed : Event<Unit, Unit>(Unit, null)
    class OpenedMapClosed(body: Dmm) : Event<Dmm, Unit>(body, null)
    class SelectedMapZSelectedChanged(zSelected: Int) : Event<Int, Unit>(zSelected, null)
    class SelectedMapMapSizeChanged(body: MapSize) : Event<MapSize, Unit>(body, null)

    class MapMousePosChanged(body: MapPos) : Event<MapPos, Unit>(body, null)
    class MapMouseDragStarted : Event<Unit, Unit>(Unit, null)
    class MapMouseDragStopped : Event<Unit, Unit>(Unit, null)

    class FrameRefreshed : Event<Unit, Unit>(Unit, null)
    class ActionStatusChanged(body: ActionStatus) : Event<ActionStatus, Unit>(body, null)
    class SelectedTileItemChanged(body: TileItem?) : Event<TileItem?, Unit>(body, null)
    class LayersFilterRefreshed(dmeItemTypes: Set<String>) : Event<Set<String>, Unit>(dmeItemTypes, null)
    class SelectedToolChanged(body: ToolType) : Event<ToolType, Unit>(body, null)
    class ShortcutTriggered(body: Shortcut) : Event<Shortcut, Unit>(body, null)

    class TilePopupOpened : Event<Unit, Unit>(Unit, null)
    class TilePopupClosed : Event<Unit, Unit>(Unit, null)

    class ScreenshotTakeStarted : Event<Unit, Unit>(Unit, null)
    class ScreenshotTakeStopped : Event<Unit, Unit>(Unit, null)
}
