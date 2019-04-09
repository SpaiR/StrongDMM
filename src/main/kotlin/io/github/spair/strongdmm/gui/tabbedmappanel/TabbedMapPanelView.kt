package io.github.spair.strongdmm.gui.tabbedmappanel

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasView
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class TabbedMapPanelView : View {

    private val mapCanvasView by diInstance<MapCanvasView>()

    override fun init(): JComponent {
        return JPanel().apply {
            layout = BorderLayout()
            add(mapCanvasView.init())
        }
    }
}
