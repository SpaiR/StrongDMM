package strongdmm.ui.panel.environment_tree

import imgui.ImGui
import strongdmm.Processable
import strongdmm.Ui
import strongdmm.ui.UiConstant
import strongdmm.window.Window

class EnvironmentTreePanelUi : Ui, Processable {
    companion object {
        val posX: Float
            get() = UiConstant.ELEMENT_MARGIN
        val posY: Float
            get() = ImGui.getFrameHeight() + UiConstant.ELEMENT_MARGIN

        val width: Float
            get() = 330f * Window.pointSize
        val height: Float
            get() = Window.windowHeight * .6f - posY
    }

    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
    }

    override fun process() {
        view.process()
    }
}
