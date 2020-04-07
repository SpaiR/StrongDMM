package strongdmm.ui

import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import strongdmm.StrongDMM
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.type.Reaction
import strongdmm.window.AppWindow

class WindowTitleUi : EventConsumer {
    init {
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        glfwSetWindowTitle(AppWindow.window, "${event.body.mapName} [${event.body.mapPath.readable}] - ${StrongDMM.TITLE}")
    }

    private fun handleSelectedMapClosed() {
        glfwSetWindowTitle(AppWindow.window, StrongDMM.TITLE)
    }

    private fun handleEnvironmentReset() {
        glfwSetWindowTitle(AppWindow.window, StrongDMM.TITLE)
    }
}
