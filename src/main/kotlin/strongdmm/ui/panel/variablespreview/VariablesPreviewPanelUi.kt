package strongdmm.ui.panel.variablespreview

import strongdmm.application.Processable
import strongdmm.application.Ui

class VariablesPreviewPanelUi : Ui, Processable {
    private val state = State()
    private val view = View(state)
    private val eventController = EventController(state)

    override fun process() {
        view.process()
    }
}
