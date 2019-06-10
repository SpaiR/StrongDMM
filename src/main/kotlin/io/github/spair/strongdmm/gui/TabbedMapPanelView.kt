package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.map.Dmm
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.EmptyBorder

object TabbedMapPanelView : View {

    private val indexHashList = mutableListOf<Int>()
    private val mapTabs = JTabbedPane().apply {
        isFocusable = false

        addChangeListener {
            if (indexHashList.isNotEmpty() && !isMiscEvent && selectedIndex != -1) {
                MapView.openMap(indexHashList[selectedIndex])
                MenuBarView.updateUndoable()
            }
            isMiscEvent = false
        }
    }

    private var isMiscEvent = false

    override fun initComponent(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(mapTabs, BorderLayout.NORTH)
            add(MapView.initComponent())
        }
    }

    fun addMapTab(dmm: Dmm) {
        val tabContent = JPanel(FlowLayout(FlowLayout.CENTER, 4, 0)).apply {
            isOpaque = false
        }

        val mapHash = dmm.hashCode()
        val tabIndex = mapTabs.tabCount

        indexHashList.add(mapHash)

        tabContent.add(JLabel(dmm.mapName))
        tabContent.add(JButton("x").apply {
            border = EmptyBorder(0, 4, 0, 4)
            isContentAreaFilled = false

            addActionListener {
                isMiscEvent = true
                Environment.closeMap(dmm)
                mapTabs.remove(indexHashList.indexOf(mapHash))
                indexHashList.remove(mapHash)
            }
        })

        SwingUtilities.invokeLater {
            isMiscEvent = true

            mapTabs.addTab(null, JPanel().apply {
                Dimension(Int.MAX_VALUE, 0).let {
                    size = it
                    preferredSize = it
                    maximumSize = it
                }
            })

            mapTabs.setTabComponentAt(tabIndex, tabContent)
            mapTabs.selectedIndex = mapTabs.tabCount - 1
        }
    }
}
