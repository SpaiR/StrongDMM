package strongdmm.event

import glm_.vec2.Vec2i
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.Tile
import strongdmm.controller.frame.FrameMesh

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
        class Open(body: String, callback: ((Boolean) -> Unit)) : Event<String, Boolean>(body, callback)
        class Fetch(callback: ((Dme) -> Unit)) : Event<Unit, Dme>(Unit, callback)
    }

    sealed class MapController {
        class Open(body: String) : Event<String, Unit>(body, null)
        class Close(body: Int) : Event<Int, Unit>(body, null)
        class FetchSelected(callback: ((Dmm?) -> Unit)) : Event<Unit, Dmm?>(Unit, callback)
        class FetchOpened(callback: ((Set<Dmm>) -> Unit)?) : Event<Unit, Set<Dmm>>(Unit, callback)
        class Switch(body: Int) : Event<Int, Unit>(body, null)
        class FetchAvailable(callback: ((Set<Pair<String, String>>) -> Unit)?) : Event<Unit, Set<Pair<String, String>>>(Unit, callback)
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
