package strongdmm.ui

import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import strongdmm.StrongDMM
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.type.Reaction
import strongdmm.window.AppWindow

class WindowTitleUi : EventConsumer {
    private var selectedMapId: Int = Dmm.MAP_ID_NONE

    init {
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        selectedMapId = event.body.id
        glfwSetWindowTitle(AppWindow.window, "${event.body.mapName} [${event.body.mapPath.readable}] - ${StrongDMM.TITLE}")
    }

    private fun handleOpenedMapClosed(event: Event<Dmm, Unit>) {
        if (selectedMapId == event.body.id) {
            selectedMapId = Dmm.MAP_ID_NONE
            glfwSetWindowTitle(AppWindow.window, StrongDMM.TITLE)
        }
    }

    private fun handleEnvironmentReset() {
        selectedMapId = Dmm.MAP_ID_NONE
        glfwSetWindowTitle(AppWindow.window, StrongDMM.TITLE)
    }
}
