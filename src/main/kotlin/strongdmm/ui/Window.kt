package strongdmm.ui

import imgui.Cond

abstract class Window {
    private var windowWidth: Int = -1
    private var windowHeight: Int = -1

    // Thus user can move a window as he want, but on resize everything will back to its initial state.
    fun getOptionCondition(windowWidth: Int, windowHeight: Int): Cond {
        val cond = if (this.windowWidth != windowWidth || this.windowHeight != windowHeight) Cond.Always else Cond.Once

        this.windowWidth = windowWidth
        this.windowHeight = windowHeight

        return cond
    }
}
