package strongdmm.event

import glm_.vec2.Vec2i
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapId
import strongdmm.byond.dmm.Tile
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
 */
sealed class Event<T, R>(
    val body: T,
    private val callback: ((R) -> Unit)?
) {
    sealed class Global {
        class ResetEnvironment : Event<Unit, Unit>(Unit, null)
        class SwitchMap(body: Dmm) : Event<Dmm, Unit>(body, null)
        class SwitchEnvironment(body: Dme) : Event<Dme, Unit>(body, null)
        class UpdMapMousePos(body: Vec2i) : Event<Vec2i, Unit>(body, null)
        class CloseMap(body: Dmm) : Event<Dmm, Unit>(body, null)
        class RefreshFrame : Event<Unit, Unit>(Unit, null)
        class ModalBlock(body: Boolean) : Event<Boolean, Unit>(body, null)
    }

    sealed class EnvironmentController {
        class Open(body: AbsPath, callback: ((Boolean) -> Unit)) : Event<AbsPath, Boolean>(body, callback)
        class Fetch(callback: ((Dme) -> Unit)) : Event<Unit, Dme>(Unit, callback)
    }

    sealed class MapController {
        class Open(body: AbsPath) : Event<AbsPath, Unit>(body, null)
        class Close(body: MapId) : Event<MapId, Unit>(body, null)
        class FetchSelected(callback: ((Dmm?) -> Unit)) : Event<Unit, Dmm?>(Unit, callback)
        class FetchOpened(callback: ((Set<Dmm>) -> Unit)?) : Event<Unit, Set<Dmm>>(Unit, callback)
        class Switch(body: MapId) : Event<MapId, Unit>(body, null)
        class FetchAvailable(callback: ((Set<Pair<AbsPath, RelPath>>) -> Unit)?) : Event<Unit, Set<Pair<AbsPath, RelPath>>>(Unit, callback)
    }

    sealed class FrameController {
        class Compose(callback: ((List<FrameMesh>) -> Unit)) : Event<Unit, List<FrameMesh>>(Unit, callback)
    }

    sealed class AvailableMapsDialogUi {
        class Open : Event<Unit, Unit>(Unit, null)
    }

    sealed class TilePopupUi {
        class Open(body: Tile) : Event<Tile, Unit>(body, null)
        class Close : Event<Unit, Unit>(Unit, null)
    }

    sealed class EditVarsDialogUi {
        class Open(body: Pair<Tile, Int>) : Event<Pair<Tile, Int>, Unit>(body, null)
    }

    fun reply(response: R) {
        callback?.invoke(response)
    }
}
