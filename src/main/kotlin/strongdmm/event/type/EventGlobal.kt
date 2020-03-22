package strongdmm.event.type

import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.ActionStatus
import strongdmm.controller.shortcut.Shortcut
import strongdmm.controller.tool.ToolType
import strongdmm.event.DmeItemType
import strongdmm.event.Event

abstract class EventGlobal {
    class EnvironmentReset : Event<Unit, Unit>(Unit, null)
    class EnvironmentChanged(body: Dme) : Event<Dme, Unit>(body, null)

    class SelectedMapChanged(body: Dmm) : Event<Dmm, Unit>(body, null)
    class OpenedMapClosed(body: Dmm) : Event<Dmm, Unit>(body, null)

    class MapMousePosChanged(body: MapPos) : Event<MapPos, Unit>(body, null)
    class MapMouseDragStarted : Event<Unit, Unit>(Unit, null)
    class MapMouseDragStopped : Event<Unit, Unit>(Unit, null)

    class FrameRefreshed : Event<Unit, Unit>(Unit, null)
    class ActionStatusChanged(body: ActionStatus) : Event<ActionStatus, Unit>(body, null)
    class ActiveTileItemChanged(body: TileItem?) : Event<TileItem?, Unit>(body, null)
    class LayersFilterRefreshed(body: Set<DmeItemType>) : Event<Set<DmeItemType>, Unit>(body, null)
    class ActiveToolChanged(body: ToolType) : Event<ToolType, Unit>(body, null)
    class ShortcutTriggered(body: Shortcut) : Event<Shortcut, Unit>(body, null)
}
