package strongdmm.ui.panel.objects

import strongdmm.Processable
import strongdmm.Ui
import strongdmm.ui.UiConstant
import strongdmm.ui.panel.environment_tree.EnvironmentTreePanelUi
import strongdmm.window.Window

class ObjectsPanelUi : Ui, Processable {
    companion object {
        val posX: Float
            get() = EnvironmentTreePanelUi.posX
        val posY: Float
            get() = EnvironmentTreePanelUi.posY + EnvironmentTreePanelUi.height + UiConstant.ELEMENT_MARGIN

        val width: Float
            get() = EnvironmentTreePanelUi.width
        val height: Float
            get() = Window.windowHeight - posY - UiConstant.ELEMENT_MARGIN
    }

    private val state = State()
    private val view = View(state)
    private val viewController = ViewController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
        eventController.viewController = viewController
    }

    override fun process() {
        view.process()
    }
}
