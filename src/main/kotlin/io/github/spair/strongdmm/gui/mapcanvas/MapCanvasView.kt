package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.logic.EnvCleanable
import io.github.spair.strongdmm.logic.map.Dmm
import java.awt.BorderLayout
import java.awt.Canvas
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

class MapCanvasView : View, EnvCleanable {

    val canvas = Canvas().apply { isVisible = true }

    private var tilePopup: JPopupMenu? = null
    private val mapGLRenderer = MapGLRenderer(this)

    override fun clean() {
        tryCloseTilePopup()
        mapGLRenderer.selectedMap = null
    }

    override fun initComponent(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(canvas)
        }
    }

    override fun initLogic() {
        SwingUtilities.invokeLater {
            // Update frames on simple window resize
            canvas.addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    Frame.update()
                }
            })

            // Update frames when window minimized/maximized
            PrimaryFrame.addWindowStateListener {
                Frame.update()
            }
        }
    }

    fun openMap(dmm: Dmm) {
        mapGLRenderer.switchMap(dmm)
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

    fun getSelectedMap() = mapGLRenderer.selectedMap
}
