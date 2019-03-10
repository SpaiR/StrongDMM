package io.github.spair.strongdmm.gui.view

import io.github.spair.strongdmm.DI
import org.kodein.di.erased.instance
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class LeftScreenView : View {

    private val objectTreeView by DI.instance<ObjectTreeView>()

    override fun init(): JComponent {
        return JPanel().apply {
            layout = BorderLayout()
            preferredSize = Dimension(350, Int.MAX_VALUE)
            add(JScrollPane(objectTreeView.init()))
        }
    }
}
