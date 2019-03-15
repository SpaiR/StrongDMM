package io.github.spair.strongdmm.gui.common

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasView
import org.kodein.di.erased.instance
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class RightScreenView : View {

    private val mapCanvasView by DI.instance<MapCanvasView>()

    override fun init(): JComponent {
        return JPanel().apply {
            layout = BorderLayout()
            add(mapCanvasView.init())
        }
    }
}
