package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasView
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class TabbedMapPanelView : View {

    private val mapCanvasView by diInstance<MapCanvasView>()

    override fun initComponent(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(mapCanvasView.initComponent())
        }
    }
}
