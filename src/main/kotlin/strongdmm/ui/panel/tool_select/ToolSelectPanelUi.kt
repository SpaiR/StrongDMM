package strongdmm.ui.panel.tool_select

import strongdmm.Processable

class ToolSelectPanelUi : Processable {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController()
    private val shortcutController = ShortcutController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
        shortcutController.viewController = viewController
    }

    override fun process() {
        view.process()
    }
}
