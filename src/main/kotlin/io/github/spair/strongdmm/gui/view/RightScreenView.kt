package io.github.spair.strongdmm.gui.view

import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class RightScreenView : View {

    override fun init(): JComponent {
        val panel = JPanel()
        panel.add(JLabel("Right label"))
        return panel
    }
}
