package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class CenterScreenView : View {

    private val mapCanvasView by diInstance<MapCanvasView>()

    override fun init(): JComponent {
        return JPanel().apply {
            layout = BorderLayout()
            add(mapCanvasView.init())
        }
    }
}

class LeftScreenView : View {

    private val objectTreeView by diInstance<ObjectTreeView>()

    override fun init(): JComponent {
        return JPanel().apply {
            layout = BorderLayout()
            preferredSize = Dimension(350, Int.MAX_VALUE)
            add(JScrollPane(objectTreeView.init()))
        }
    }
}
