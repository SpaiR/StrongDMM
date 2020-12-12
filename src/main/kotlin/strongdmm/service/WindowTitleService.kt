package strongdmm.service

import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import strongdmm.StrongDMM
import strongdmm.application.Service
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.application.window.Window
import strongdmm.event.service.ReactionEnvironmentService
import strongdmm.event.service.ReactionMapHolderService

class WindowTitleService : Service {
    private var environmentName: String = ""

    init {
        EventBus.sign(ReactionMapHolderService.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        EventBus.sign(ReactionEnvironmentService.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
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
