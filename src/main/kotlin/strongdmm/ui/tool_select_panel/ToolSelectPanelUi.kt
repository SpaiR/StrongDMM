package strongdmm.ui.tool_select_panel

class ToolSelectPanelUi {
    private val state = State()
    private val view = View(state)
    private val viewController = ViewController()
    private val shortcutController = ShortcutController(state)
    private val eventController = EventController(state)

    init {
        view.viewController = viewController
        shortcutController.viewController = viewController
    }

    fun process() {
        view.process()
    }
}
