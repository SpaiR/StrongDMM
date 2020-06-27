package strongdmm.ui.panel.coords

import imgui.ImGui
import strongdmm.Processable
import strongdmm.Ui
import strongdmm.ui.UiConstant
import strongdmm.window.Window

class CoordsPanelUi : Ui, Processable {
    companion object {
        val posX: Float
            get() = Window.windowWidth - width - UiConstant.ELEMENT_MARGIN
        val posY: Float
            get() = Window.windowHeight - height - UiConstant.ELEMENT_MARGIN

        val width: Float
            get() = 6.5f * ImGui.getFontSize()
        val height: Float
            get() = 2f * ImGui.getFontSize()
    }

    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
