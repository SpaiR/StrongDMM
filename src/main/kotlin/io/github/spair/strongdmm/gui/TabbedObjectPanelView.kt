package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.EmptyBorder

class TabbedObjectPanelView : View {

    private val objectTreeView by diInstance<ObjectTreeView>()
    private val instanceListView by diInstance<InstanceListView>()

    private val tabbedPanel = JTabbedPane()

    private val typeField = JTextField("no type selected", 30).apply {
        isEditable = false
        border = EmptyBorder(0, 0, 0, 0)
    }

    override fun initComponent(): JComponent {
        return JPanel(BorderLayout()).apply {
            preferredSize = Dimension(350, Int.MAX_VALUE)
            add(tabbedPanel.apply {
                addTab("Tree", objectTreeView.initComponent())
                addTab("Instance (empty)", instanceListView.initComponent())
            })
            add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(JLabel("<html><b>Type:</b></html>"))
                add(typeField)
            }, BorderLayout.SOUTH)
        }
    }

    fun setInstanceCount(count: Int) {
        tabbedPanel.setTitleAt(1, "Instance ($count)")
    }

    fun setType(type: String) {
        typeField.text = type
    }
}
