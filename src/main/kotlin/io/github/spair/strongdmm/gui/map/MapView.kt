package io.github.spair.strongdmm.gui.map

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
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

    private val canvas = Canvas().apply { isVisible = true }

    private var tilePopup: JPopupMenu? = null
    private val pipeline = MapPipeline(canvas)

    override fun clean() {
        tryCloseTilePopup()
        pipeline.selectedMapData = null
        pipeline.openedMaps.clear()
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
        pipeline.mapLoadingInProcess = true
        pipeline.switchMap(dmm)
        InstanceListView.updateSelectedInstanceInfo()
    }

    fun closeMap(hash: Int) {
        pipeline.closeMap(hash)
        InstanceListView.updateSelectedInstanceInfo()
    }

    fun openMap(hash: Int) {
        pipeline.switchMap(hash)
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

    fun getSelectedDmm() = pipeline.selectedMapData?.dmm
    fun getOpenedMaps(): List<Dmm> = pipeline.openedMaps.values.map { it.dmm }

    fun switchMapsSync() {
        with(pipeline) {
            synchronizeMaps = !synchronizeMaps

            if (synchronizeMaps && openedMaps.size > 1) {
                selectedMapData?.let {
                    triggerMapSync(it)
                }
            }
        }
    }

    fun switchAreasFraming() {
        pipeline.drawAreasBorder = !pipeline.drawAreasBorder
        Frame.update()
    }

    fun isMapLoadingInProcess(): Boolean = pipeline.mapLoadingInProcess
}
