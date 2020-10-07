package strongdmm.ui.panel.layers_filter

import strongdmm.application.Processable
import strongdmm.application.Ui

class LayersFilterPanelUi : Ui, Processable {
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
