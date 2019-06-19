package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.logic.map.OUT_OF_BOUNDS
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

object StatusView : View {

    private val statusPanel: JPanel = JPanel(FlowLayout(FlowLayout.LEFT))

    private val xCoordLabel: JLabel = JLabel("X:###")
    private val yCoordLabel: JLabel = JLabel("Y:###")

    init {
        statusPanel.isVisible = false

        arrayOf(xCoordLabel, yCoordLabel).forEach {
            it.preferredSize = Dimension(40, 10)
        }
    }

    override fun initComponent(): JComponent {
        return statusPanel.apply {
            add(xCoordLabel)
            add(yCoordLabel)
        }
    }

    fun showStatus() {
        statusPanel.isVisible = true
    }

    fun hideStatus() {
        statusPanel.isVisible = false
    }

    fun updateCoords(x: Int, y: Int) {
        xCoordLabel.text = if (x == OUT_OF_BOUNDS) "X:###" else "X:${String.format("%03d", x)}"
        yCoordLabel.text = if (y == OUT_OF_BOUNDS) "Y:###" else "Y:${String.format("%03d", y)}"
    }
}
