package strongdmm.event

import glm_.vec2.Vec2i
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapId
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItemIdx
import strongdmm.controller.action.ActionStatus
import strongdmm.controller.action.Undoable
import strongdmm.controller.canvas.CanvasBlockStatus
import strongdmm.controller.environment.EnvOpenStatus
import strongdmm.controller.frame.FrameMesh
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
 * This restriction is checked on runtime.
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
        class SwitchMap(body: Dmm) : Event<Dmm, Unit>(body, null)
        class SwitchEnvironment(body: Dme) : Event<Dme, Unit>(body, null)
        class MapMousePosChanged(body: Vec2i) : Event<Vec2i, Unit>(body, null)
        class CloseMap(body: Dmm) : Event<Dmm, Unit>(body, null)
        class RefreshFrame : Event<Unit, Unit>(Unit, null)
        class ActionStatusChanged(body: ActionStatus) : Event<ActionStatus, Unit>(body, null)
    }

    abstract class EnvironmentController {
        class Open(body: AbsPath, callback: ((EnvOpenStatus) -> Unit)) : Event<AbsPath, EnvOpenStatus>(body, callback)
        class Fetch(callback: ((Dme) -> Unit)) : Event<Unit, Dme>(Unit, callback)
    }

    abstract class MapController {
        class Open(body: AbsPath) : Event<AbsPath, Unit>(body, null)
        class Close(body: MapId) : Event<MapId, Unit>(body, null)
        class FetchSelected(callback: ((Dmm?) -> Unit)) : Event<Unit, Dmm?>(Unit, callback)
        class FetchAllOpened(callback: ((Set<Dmm>) -> Unit)?) : Event<Unit, Set<Dmm>>(Unit, callback)
        class Switch(body: MapId) : Event<MapId, Unit>(body, null)
        class FetchAllAvailable(callback: ((Set<Pair<AbsPath, RelPath>>) -> Unit)?) : Event<Unit, Set<Pair<AbsPath, RelPath>>>(Unit, callback)
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
        class Open(body: Pair<Tile, TileItemIdx>) : Event<Pair<Tile, TileItemIdx>, Unit>(body, null)
    }

    abstract class ActionController {
        class AddAction(body: Undoable) : Event<Undoable, Unit>(body, null)
        class UndoAction : Event<Unit, Unit>(Unit, null)
        class RedoAction : Event<Unit, Unit>(Unit, null)
    }

    abstract class CanvasController {
        class Block(body: CanvasBlockStatus) : Event<CanvasBlockStatus, Unit>(body, null)
    }

    fun reply(response: R) {
        callback?.invoke(response)
    }
}
