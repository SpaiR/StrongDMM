package strongdmm.ui

import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import strongdmm.StrongDMM
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.window.AppWindow

class WindowTitleUi : EventHandler {
    private var environmentName: String = ""

    init {
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        glfwSetWindowTitle(AppWindow.windowPtr, "$environmentName [${event.body.mapPath.readable}] - ${StrongDMM.TITLE}")
    }

    private fun handleSelectedMapClosed() {
        glfwSetWindowTitle(AppWindow.windowPtr, StrongDMM.TITLE)
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        environmentName = event.body.name
        glfwSetWindowTitle(AppWindow.windowPtr, "$environmentName - ${StrongDMM.TITLE}")
    }

    private fun handleEnvironmentReset() {
        glfwSetWindowTitle(AppWindow.windowPtr, StrongDMM.TITLE)
    }
}
