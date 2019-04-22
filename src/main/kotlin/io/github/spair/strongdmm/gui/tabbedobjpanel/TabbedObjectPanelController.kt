package io.github.spair.strongdmm.gui.tabbedobjpanel

import io.github.spair.strongdmm.diDirect
import io.github.spair.strongdmm.gui.ViewController

class TabbedObjectPanelController : ViewController<TabbedObjectPanelView>(diDirect()) {

    private var instanceTabbedInitialized = false

    fun setInstanceCount(count: Int) {
        if (instanceTabbedInitialized) {
            view.changeInstanceCount(count)
        } else {
            view.initializeInstanceTab(count)
            instanceTabbedInitialized = true
        }
    }
}
