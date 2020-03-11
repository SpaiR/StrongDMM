package strongdmm.event

import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.*
import strongdmm.controller.action.undoable.Undoable
import strongdmm.controller.tool.ToolType
import strongdmm.ui.search.SearchRect
import strongdmm.ui.search.SearchResult
import java.io.File

/**
 * Events are used to do a communication between application components.
 * By design only "*Ui" and "*Controller" classes could receive them.
 * It's allowed to send events from classes different then ui or controllers, but mechanism should be used with wisdom.
 *
 * Global events are used to show that something globally happened. Like environment was switched or map was closed.
 * Unlike the others, global events could be consumed by any classes.
 *
 * Events like "EnvironmentController" are meant to be consumed ONLY by a specific class.
 * This restriction is checked in runtime.
 *
 * Sometimes it's needed to provide state from one class to another to avoid unneeded events creation.
 * "Event.Provider.*" events should be used for that.
 * Provided variables should be finalized in places they are declared.
 *
 * To make sure that events by themselves are fully self-explanatory, primitive types as well as raw strings should not be used as arguments.
 * Use typealiase instead.
 */
abstract class Event<T, R>(
    val body: T,
    private val callback: ((R) -> Unit)?
) {
    // @formatter:off
    abstract class EnvironmentController {
        class Open(body: File, callback: (EnvironmentBlockStatus) -> Unit) : Event<File, EnvironmentBlockStatus>(body, callback)
        class Fetch(callback: ((Dme) -> Unit)) : Event<Unit, Dme>(Unit, callback)
    }

    abstract class MapHolderController {
        class Open(body: File) : Event<File, Unit>(body, null)
        class Close(body: MapId) : Event<MapId, Unit>(body, null)
        class FetchSelected(callback: ((Dmm) -> Unit)) : Event<Unit, Dmm>(Unit, callback)
        class Switch(body: MapId) : Event<MapId, Unit>(body, null)
        class Save : Event<Unit, Unit>(Unit, null)
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
        class SelectArea(body: MapArea) : Event<MapArea, Unit>(body, null)
        class ResetSelectedArea : Event<Unit, Unit>(Unit, null)
        class HighlightSelectedArea : Event<Unit, Unit>(Unit, null)
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

    abstract class ToolsController {
        class Switch(body: ToolType) : Event<ToolType, Unit>(body, null)
        class Reset : Event<Unit, Unit>(Unit, null)
        class FetchActiveArea(callback: ((MapArea) -> Unit)) : Event<Unit, MapArea>(Unit, callback)
    }

    abstract class ClipboardController {
        class Copy : Event<Unit, Unit>(Unit, null)
        class Paste : Event<Unit, Unit>(Unit, null)
    }

    fun reply(response: R) {
        callback?.invoke(response)
    }
    // @formatter:on
}
