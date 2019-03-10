package io.github.spair.strongdmm.gui.view

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