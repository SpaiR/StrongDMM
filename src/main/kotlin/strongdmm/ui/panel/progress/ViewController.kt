package strongdmm.ui.panel.progress

class ViewController(
    private val state: State
) {
    companion object {
        private const val WINDOW_MOD_STEP: Float = 20f
    }

    fun isOpening(): Boolean {
        if (state.progressText != null && state.windowWidth < state.progressTextWidth) {
            state.windowWidth += WINDOW_MOD_STEP
            return true
        }
        return false
    }

    fun isClosing(): Boolean {
        if (state.progressText == null && state.windowWidth > 0) {
            state.windowWidth -= WINDOW_MOD_STEP
            return true
        }
        return false
    }
}
