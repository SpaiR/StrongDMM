package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.gui.View
import java.awt.BorderLayout
import java.awt.Canvas
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

class MapCanvasView : View {

    val canvas = Canvas().apply { isVisible = true }
    private var tilePopup: JPopupMenu? = null

    override fun init(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(canvas)
        }
    }

    fun tryCloseTilePopup(): Boolean {
        if (tilePopup != null && tilePopup!!.isVisible) {
            SwingUtilities.invokeLater {
                tilePopup?.isVisible = false
                tilePopup = null
            }
            return true
        }
        return false
    }

    fun createAndShowTilePopup(x:Int, y: Int, fillInAction: (JPopupMenu) -> Unit) {
        SwingUtilities.invokeLater {
            tilePopup = JPopupMenu().apply {
                isLightWeightPopupEnabled = false
                fillInAction(this)
                show(canvas, x, y)
            }
        }
    }
}
