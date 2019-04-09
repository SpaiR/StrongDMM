package io.github.spair.strongdmm.gui.tabbedobjpanel

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class TabbedObjectPanelView : View {

    private val objectTreeView by diInstance<ObjectTreeView>()

    override fun init(): JComponent {
        return JPanel().apply {
            layout = BorderLayout()
            preferredSize = Dimension(350, Int.MAX_VALUE)
            add(objectTreeView.init())
        }
    }
}