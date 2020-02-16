package strongdmm.event

import imgui.ImBool
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.ActionStatus
import strongdmm.controller.action.undoable.Undoable
import strongdmm.controller.frame.FrameMesh
import strongdmm.ui.search.SearchRect
import strongdmm.ui.search.SearchResult
import java.io.File

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
 * Use typealiase instead.
 */
abstract class Event<T, R>(
    val body: T,
    private val callback: ((R) -> Unit)?
) {
    // @formatter:off
    abstract class Global {
        class ResetEnvironment : Event<Unit, Unit>(Unit, null)
        class SwitchEnvironment(body: Dme) : Event<Dme, Unit>(body, null)
        class SwitchMap(body: Dmm) : Event<Dmm, Unit>(body, null)
        class CloseMap(body: Dmm) : Event<Dmm, Unit>(body, null)
        class MapMousePosChanged(body: MapPos) : Event<MapPos, Unit>(body, null)
        class MapMouseDragStart : Event<Unit, Unit>(Unit, null)
        class MapMouseDragStop : Event<Unit, Unit>(Unit, null)
        class RefreshFrame : Event<Unit, Unit>(Unit, null)
        class ActionStatusChanged(body: ActionStatus) : Event<ActionStatus, Unit>(body, null)
        class SwitchSelectedTileItem(body: TileItem) : Event<TileItem, Unit>(body, null)
        class RefreshLayersFilter(body: Set<DmeItemType>) : Event<Set<DmeItemType>, Unit>(body, null)

        abstract class Provider {
            class InstanceLocatorOpen(body: ImBool) : Event<ImBool, Unit>(body, null)
        }
    }

    abstract class EnvironmentController {
        class Open(body: File, callback: ((EnvironmentBlockStatus) -> Unit)? = null) : Event<File, EnvironmentBlockStatus>(body, callback)
        class Fetch(callback: ((Dme) -> Unit)) : Event<Unit, Dme>(Unit, callback)
    }

    abstract class MapHolderController {
        class Open(body: File) : Event<File, Unit>(body, null)
        class Close(body: MapId) : Event<MapId, Unit>(body, null)
        class FetchSelected(callback: ((Dmm?) -> Unit)) : Event<Unit, Dmm?>(Unit, callback)
        class FetchAllOpened(callback: ((Set<Dmm>) -> Unit)?) : Event<Unit, Set<Dmm>>(Unit, callback)
        class Switch(body: MapId) : Event<MapId, Unit>(body, null)
        class FetchAllAvailable(callback: ((Set<Pair<AbsoluteFilePath, VisibleFilePath>>) -> Unit)?) : Event<Unit, Set<Pair<AbsoluteFilePath, VisibleFilePath>>>(Unit, callback)
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
        class SelectTiles(body: Collection<MapPos>) : Event<Collection<MapPos>, Unit>(body, null)
        class ResetSelectedTiles : Event<Unit, Unit>(Unit, null)
    }

    abstract class ObjectPanelUi {
        class Update : Event<Unit, Unit>(Unit, null)
    }

    abstract class InstanceController {
        class GenerateFromIconStates(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)
        class GenerateFromDirections(body: TileItem, callback: (Unit) -> Unit) : Event<TileItem, Unit>(body, callback)
        class FindPositionsByType(body: Pair<SearchRect, TileItemType>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) : Event<Pair<SearchRect, TileItemType>, List<Pair<TileItem, MapPos>>>(body, callback)
        class FindPositionsById(body: Pair<SearchRect, TileItemId>, callback: (List<Pair<TileItem, MapPos>>) -> Unit) : Event<Pair<SearchRect, TileItemId>, List<Pair<TileItem, MapPos>>>(body, callback)
    }

    abstract class SearchResultPanelUi {
        class Open(body: SearchResult) : Event<SearchResult, Unit>(body, null)
    }

    abstract class InstanceLocatorPanelUi {
        class SearchByType(body: TileItemType) : Event<TileItemType, Unit>(body, null)
        class SearchById(body: TileItemId) : Event<TileItemId, Unit>(body, null)
    }

    abstract class MapModifierController {
        class ReplaceTypeInPositions(body: Pair<TileItemType, List<Pair<TileItem, MapPos>>>) : Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>(body, null)
        class ReplaceIdInPositions(body: Pair<TileItemType, List<Pair<TileItem, MapPos>>>) : Event<Pair<TileItemType, List<Pair<TileItem, MapPos>>>, Unit>(body, null)
        class DeleteTypeInPositions(body: List<Pair<TileItem, MapPos>>) : Event<List<Pair<TileItem, MapPos>>, Unit>(body, null)
        class DeleteIdInPositions(body: List<Pair<TileItem, MapPos>>) : Event<List<Pair<TileItem, MapPos>>, Unit>(body, null)
    }

    abstract class LayersFilterPanelUi {
        class Open : Event<Unit, Unit>(Unit, null)
    }

    abstract class LayersFilterController {
        class FilterById(body: DmeItemIdArray) : Event<DmeItemIdArray, Unit>(body, null)
        class ShowByType(body: DmeItemType) : Event<DmeItemType, Unit>(body, null)
        class HideByType(body: DmeItemType) : Event<DmeItemType, Unit>(body, null)
        class Fetch(callback: ((Set<DmeItemType>) -> Unit)) : Event<Unit, Set<DmeItemType>>(Unit, callback)
    }

    fun reply(response: R) {
        callback?.invoke(response)
    }
    // @formatter:on
}
