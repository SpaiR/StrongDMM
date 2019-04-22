package io.github.spair.strongdmm.gui.tabbedobjpanel

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTabbedPane

class TabbedObjectPanelView : View {

    private val tabbedPanel = JTabbedPane()

    private val objectTreeView by diInstance<ObjectTreeView>()
    private val instanceListView by diInstance<InstanceListView>()

    override fun init(): JComponent {
        return JPanel(BorderLayout()).apply {
            preferredSize = Dimension(350, Int.MAX_VALUE)
            add(tabbedPanel.apply {
                addTab("Tree", objectTreeView.init())
            })
        }
    }

    fun initializeInstanceTab(initialCount: Int) {
        tabbedPanel.addTab("Instance ($initialCount)", instanceListView.init())
    }

    fun changeInstanceCount(count: Int) {
        tabbedPanel.setTitleAt(1, "Instance ($count)")
    }
}
