package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTabbedPane

class TabbedObjectPanelView : View {

    private val objectTreeView by diInstance<ObjectTreeView>()
    private val instanceListView by diInstance<InstanceListView>()

    private val tabbedPanel = JTabbedPane()

    override fun initComponent(): JComponent {
        return JPanel(BorderLayout()).apply {
            preferredSize = Dimension(350, Int.MAX_VALUE)
            add(tabbedPanel.apply {
                addTab("Tree", objectTreeView.initComponent())
                addTab("Instance (empty)", instanceListView.initComponent())
            })
        }
    }

    fun setInstanceCount(count: Int) {
        tabbedPanel.setTitleAt(1, "Instance ($count)")
    }
}
