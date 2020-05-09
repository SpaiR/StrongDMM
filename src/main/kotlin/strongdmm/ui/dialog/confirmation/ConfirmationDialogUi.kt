package strongdmm.ui.dialog.confirmation

import strongdmm.Processable
import strongdmm.Ui

class ConfirmationDialogUi : Ui, Processable {
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
