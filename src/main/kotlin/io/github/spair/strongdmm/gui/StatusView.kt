package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.common.OUT_OF_BOUNDS
import io.github.spair.strongdmm.gui.common.BorderUtil
import io.github.spair.strongdmm.gui.common.View
import java.awt.Color
import java.awt.Font
import javax.swing.*
import javax.swing.BorderFactory.createCompoundBorder

object StatusView : View {

    private val statusPanel: JPanel = JPanel()

    private val xCoordLabel: JLabel = JLabel("X:000")
    private val yCoordLabel: JLabel = JLabel("Y:000")
    private val loadingLabel: JLabel = JLabel("Loading...")

    init {
        with(statusPanel) {
            layout = BoxLayout(this, BoxLayout.LINE_AXIS)
            border = createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderUtil.createEmptyBorder(5)
            )
        }

        val font = loadingLabel.font.deriveFont(Font.PLAIN, 10f)

        with(loadingLabel) {
            this.font = font
            icon = ImageIcon(StatusView::class.java.classLoader.getResource("loader.gif"))
            isVisible = false
        }

        arrayOf(xCoordLabel, yCoordLabel).forEach {
            it.font = font
        }
    }

    override fun initComponent(): JComponent {
        return statusPanel.apply {
            add(xCoordLabel)
            add(Box.createHorizontalStrut(5))
            add(yCoordLabel)
            add(Box.createHorizontalGlue())
            add(loadingLabel)
        }
    }

    fun updateCoords(x: Int, y: Int) {
        xCoordLabel.text = if (x == OUT_OF_BOUNDS) "X:000" else "X:${String.format("%03d", x)}"
        yCoordLabel.text = if (y == OUT_OF_BOUNDS) "Y:000" else "Y:${String.format("%03d", y)}"
    }

    fun showLoader(loadingText: String = "Loading...") {
        loadingLabel.text = loadingText
        loadingLabel.isVisible = true
    }

    fun hideLoader() {
        loadingLabel.isVisible = false
    }
}
