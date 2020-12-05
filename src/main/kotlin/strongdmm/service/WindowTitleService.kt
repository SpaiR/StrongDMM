package strongdmm.service

import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import strongdmm.StrongDMM
import strongdmm.application.Service
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.application.window.Window

class WindowTitleService : Service {
    private var environmentName: String = ""

    init {
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        EventBus.sign(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        glfwSetWindowTitle(Window.ptr, "$environmentName [${event.body.mapPath.readable}] - ${StrongDMM.TITLE}")
    }

    private fun handleSelectedMapClosed() {
        glfwSetWindowTitle(Window.ptr, StrongDMM.TITLE)
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        environmentName = event.body.name
        glfwSetWindowTitle(Window.ptr, "$environmentName - ${StrongDMM.TITLE}")
    }

    private fun handleEnvironmentReset() {
        glfwSetWindowTitle(Window.ptr, StrongDMM.TITLE)
    }
}
