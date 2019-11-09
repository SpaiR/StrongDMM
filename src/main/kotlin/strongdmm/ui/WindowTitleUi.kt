package strongdmm.ui

import StrongDmm
import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import uno.glfw.glfw

class WindowTitleUi : EventConsumer {
    private var selectedMapId: Int = -1

    init {
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
    }

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        selectedMapId = event.body.id
        glfwSetWindowTitle(glfw.currentContext.L, "${event.body.mapName} [${event.body.relativeMapPath}] - ${StrongDmm.TITLE}")
    }

    private fun handleCloseMap(event: Event<Dmm, Unit>) {
        if (selectedMapId == event.body.id) {
            selectedMapId = -1
            glfwSetWindowTitle(glfw.currentContext.L, StrongDmm.TITLE)
        }
    }

    private fun handleResetEnvironment() {
        selectedMapId = -1
        glfwSetWindowTitle(glfw.currentContext.L, StrongDmm.TITLE)
    }
}
