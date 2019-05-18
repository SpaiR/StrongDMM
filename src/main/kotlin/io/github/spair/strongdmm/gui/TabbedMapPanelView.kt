package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.gui.map.MapView
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

object TabbedMapPanelView : View {
    override fun initComponent(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(MapView.initComponent())
        }
    }
}
