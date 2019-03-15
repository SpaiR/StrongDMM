package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.gui.common.View
import java.awt.BorderLayout
import java.awt.Canvas
import javax.swing.JComponent
import javax.swing.JPanel

class MapCanvasView : View {

    val canvas = Canvas().apply { isVisible = true }

    override fun init(): JComponent {
        return JPanel().apply {
            layout = BorderLayout()
            add(canvas)
        }
    }
}