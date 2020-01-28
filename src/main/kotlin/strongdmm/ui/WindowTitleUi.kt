package strongdmm.ui

import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import strongdmm.StrongDMM
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.window.AppWindow

class WindowTitleUi : EventConsumer {
    private var selectedMapId: Int = Dmm.MAP_ID_NONE

    init {
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
    }

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        selectedMapId = event.body.id
        glfwSetWindowTitle(AppWindow.window, "${event.body.mapName} [${event.body.visibleMapPath}] - ${StrongDMM.TITLE}")
    }

    private fun handleCloseMap(event: Event<Dmm, Unit>) {
        if (selectedMapId == event.body.id) {
            selectedMapId = Dmm.MAP_ID_NONE
            glfwSetWindowTitle(AppWindow.window, StrongDMM.TITLE)
        }
    }

    private fun handleResetEnvironment() {
        selectedMapId = Dmm.MAP_ID_NONE
        glfwSetWindowTitle(AppWindow.window, StrongDMM.TITLE)
    }
}
