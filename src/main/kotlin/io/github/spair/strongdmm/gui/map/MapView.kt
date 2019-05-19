package io.github.spair.strongdmm.gui.map

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.select.AddTileSelect
import io.github.spair.strongdmm.gui.map.select.FillTileSelect
import io.github.spair.strongdmm.gui.map.select.SelectType
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

object MapView : View, EnvCleanable {

    val canvas = Canvas().apply { isVisible = true }

    private var tilePopup: JPopupMenu? = null
    private val pipeline = MapPipeline(this)

    override fun clean() {
        tryCloseTilePopup()
        pipeline.selectedMap = null
    }

    override fun initComponent(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(canvas)

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
    }

    fun openMap(dmm: Dmm) {
        pipeline.switchMap(dmm)
        InstanceListView.updateSelectedInstanceInfo()
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

    fun switchSelectMode(selectType: SelectType) {
        when (selectType) {
            SelectType.ADD -> pipeline.tileSelect = AddTileSelect()
            SelectType.FILL -> pipeline.tileSelect = FillTileSelect()
        }
    }

    fun getSelectedMap() = pipeline.selectedMap
}
