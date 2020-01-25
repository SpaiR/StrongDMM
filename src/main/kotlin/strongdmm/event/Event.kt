package strongdmm.event

import imgui.ImBool
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.*
import strongdmm.controller.action.ActionStatus
import strongdmm.controller.action.Undoable
import strongdmm.controller.canvas.CanvasBlockStatus
import strongdmm.controller.environment.EnvOpenStatus
import strongdmm.controller.frame.FrameMesh
import strongdmm.ui.search.SearchResult
import strongdmm.util.inline.AbsPath
import strongdmm.util.inline.RelPath

/**
 * Events are used to do a communication between application components.
 * By design only "*Ui" and "*Controller" classes should be able to send and receive them.
 *
 * Global events are used to show that something globally happened. Like environment switching or map closing.
 * Unlike the others, global events could be consumed by any number of classes.
 *
 * Events like "EnvironmentController" are meant to be consumed ONLY by a specific class.
 * This restriction is checked in runtime.
 *
 * Sometimes it's needed to provide state with ImGui pointer-like variables from one UI to another.
 * To do that use 'Event.Global.Provider' subclasses.
 * The only types could be send through those events are:
 *  - ImBool
 *
 * !!! IMPORTANT !!!
 * To make sure that events by themselves are fully self-explanatory, primitive types as well as raw strings should not be used as arguments.
 * Inline classes should be used instead.
 */
abstract class Event<T, R>(
    val body: T,
    private val callback: ((R) -> Unit)?
) {
    abstract class Global {
        class ResetEnvironment : Event<Unit, Unit>(Unit, null)
        class SwitchEnvironment(body: Dme) : Event<Dme, Unit>(body, null)
        class SwitchMap(body: Dmm) : Event<Dmm, Unit>(body, null)
        class CloseMap(body: Dmm) : Event<Dmm, Unit>(body, null)
        class MapMousePosChanged(body: MapPos) : Event<MapPos, Unit>(body, null)
        class RefreshFrame : Event<Unit, Unit>(Unit, null)
        class ActionStatusChanged(body: ActionStatus) : Event<ActionStatus, Unit>(body, null)
        class SwitchSelectedTileItem(body: TileItem) : Event<TileItem, Unit>(body, null)

        abstract class Provider {
            class InstanceLocatorOpen(body: ImBool) : Event<ImBool, Unit>(body, null)
        }
    }

    abstract class EnvironmentController {
        class Open(body: AbsPath, callback: ((EnvOpenStatus) -> Unit)? = null) : Event<AbsPath, EnvOpenStatus>(body, callback)
        class Fetch(callback: ((Dme) -> Unit)) : Event<Unit, Dme>(Unit, callback)
    }

    abstract class MapController {
        class Open(body: AbsPath) : Event<AbsPath, Unit>(body, null)
        class Close(body: MapId) : Event<MapId, Unit>(body, null)
        class FetchSelected(callback: ((Dmm?) -> Unit)) : Event<Unit, Dmm?>(Unit, callback)
        class FetchAllOpened(callback: ((Set<Dmm>) -> Unit)?) : Event<Unit, Set<Dmm>>(Unit, callback)
        class Switch(body: MapId) : Event<MapId, Unit>(body, null)
        class FetchAllAvailable(callback: ((Set<Pair<AbsPath, RelPath>>) -> Unit)?) : Event<Unit, Set<Pair<AbsPath, RelPath>>>(Unit, callback)
        class Save : Event<Unit, Unit>(Unit, null)
    }

    abstract class FrameController {
        class Compose(callback: ((List<FrameMesh>) -> Unit)) : Event<Unit, List<FrameMesh>>(Unit, callback)
    }

    abstract class AvailableMapsDialogUi {
        class Open : Event<Unit, Unit>(Unit, null)
    }

    abstract class TilePopupUi {
        class Open(body: Tile) : Event<Tile, Unit>(body, null)
        class Close : Event<Unit, Unit>(Unit, null)
    }

    abstract class EditVarsDialogUi {
        class OpenWithTile(body: Pair<Tile, TileItemIdx>) : Event<Pair<Tile, TileItemIdx>, Unit>(body, null)
        class OpenWithTileItem(body: TileItem) : Event<TileItem, Unit>(body, null)
    }

    abstract class ActionController {
        class AddAction(body: Undoable) : Event<Undoable, Unit>(body, null)
        class UndoAction : Event<Unit, Unit>(Unit, null)
        class RedoAction : Event<Unit, Unit>(Unit, null)
    }

    abstract class CanvasController {
        class Block(body: CanvasBlockStatus) : Event<CanvasBlockStatus, Unit>(body, null)
        class CenterPosition(body: MapPos) : Event<MapPos, Unit>(body, null)
        class MarkPosition(body: MapPos) : Event<MapPos, Unit>(body, null)
        class ResetMarkedPosition : Event<Unit, Unit>(Unit, null)
    }

    abstract class ObjectPanelUi {
        class Update : Event<Unit, Unit>(Unit, null)
    }

    abstract class InstanceController {
        class GenerateFromIconStates(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)
        class GenerateFromDirections(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)
        class FindPositionsByType(body: TileItemType, callback: (List<Pair<TileItemType, MapPos>>) -> Unit) : Event<TileItemType, List<Pair<TileItemType, MapPos>>>(body, callback)
        class FindPositionsById(body: TileItemId, callback: (List<Pair<TileItemType, MapPos>>) -> Unit) : Event<TileItemId, List<Pair<TileItemType, MapPos>>>(body, callback)
    }

    abstract class SearchResultPanelUi {
        class Open(body: SearchResult) : Event<SearchResult, Unit>(body, null)
    }

    abstract class InstanceLocatorPanelUi {
        class SearchByType(body: TileItemType) : Event<TileItemType, Unit>(body, null)
        class SearchById(body: TileItemId) : Event<TileItemId, Unit>(body, null)
    }

    fun reply(response: R) {
        callback?.invoke(response)
    }
}
