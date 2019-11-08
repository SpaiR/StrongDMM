package strongdmm.ui

import StrongDmm
import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.Message
import uno.glfw.glfw

class WindowTitleUi : EventConsumer {
    private var selectedMapId: Int = -1

    init {
        consumeEvent(Event.GLOBAL_SWITCH_MAP, ::handleSwitchMap)
        consumeEvent(Event.GLOBAL_CLOSE_MAP, ::handleCloseMap)
        consumeEvent(Event.GLOBAL_RESET_ENVIRONMENT, ::handleResetEnvironment)
    }

    private fun handleSwitchMap(msg: Message<Dmm, Unit>) {
        selectedMapId = msg.body.id
        glfwSetWindowTitle(glfw.currentContext.L, "${msg.body.mapName} [${msg.body.relativeMapPath}] - ${StrongDmm.TITLE}")
    }

    private fun handleCloseMap(msg: Message<Dmm, Unit>) {
        if (selectedMapId == msg.body.id) {
            selectedMapId = -1
            glfwSetWindowTitle(glfw.currentContext.L, StrongDmm.TITLE)
        }
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        selectedMapId = -1
        glfwSetWindowTitle(glfw.currentContext.L, StrongDmm.TITLE)
    }
}
