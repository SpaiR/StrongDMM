package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.logic.EnvCleanable
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.map.Dmm
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*
import javax.swing.border.EmptyBorder

object TabbedMapPanelView : View, EnvCleanable {

    private val mapTabs: JTabbedPane = JTabbedPane()
    private val indexHashList: MutableList<Int> = mutableListOf()

    private var isMiscEvent: Boolean = false
    private var previousIndex: Int = -1

    init {
        with(mapTabs) {
            isFocusable = false

            addChangeListener {
                if (indexHashList.isNotEmpty() && !isMiscEvent && selectedIndex != -1) {
                    MapView.openMap(indexHashList[selectedIndex])
                    MenuBarView.updateUndoable()
                }

                getTab(previousIndex)?.toggleBoldFont(false)
                previousIndex = selectedIndex
                getTab(selectedIndex)?.toggleBoldFont(true)

                isMiscEvent = false
            }
        }
    }

    override fun initComponent(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(mapTabs, BorderLayout.NORTH)
            add(MapView.initComponent())
        }
    }

    override fun clean() {
        mapTabs.removeAll()
        indexHashList.clear()
    }

    fun addMapTab(dmm: Dmm) {
        val mapTab = MapTab(dmm)
        val tabIndex = mapTabs.tabCount

        getTab(previousIndex)?.toggleBoldFont(false)

        previousIndex = tabIndex
        indexHashList.add(dmm.hashCode())

        SwingUtilities.invokeLater {
            isMiscEvent = true

            mapTabs.addTab(null, JPanel().apply {
                Dimension(Int.MAX_VALUE, 0).let {
                    size = it
                    preferredSize = it
                    maximumSize = it
                }
            })

            mapTabs.setTabComponentAt(tabIndex, mapTab)
            mapTabs.selectedIndex = mapTabs.tabCount - 1
        }
    }

    fun markMapModified(isModified: Boolean) {
        getTab(mapTabs.selectedIndex)?.toggleModifiedMark(isModified)
    }

    private fun getTab(index: Int): MapTab? {
        return if (index < 0 || index >= mapTabs.tabCount) null else mapTabs.getTabComponentAt(index) as? MapTab
    }

    private class MapTab(dmm: Dmm) : JPanel(FlowLayout(FlowLayout.CENTER, 4, 0)) {

        private val plainFont: Font = font.deriveFont(Font.PLAIN)
        private val boldFont: Font = font.deriveFont(Font.BOLD)

        private val label: JLabel = JLabel(dmm.mapName)
        private val closeButton: JButton = JButton("x")

        private var hasModifiedMark: Boolean = false

        init {
            isOpaque = false

            with(closeButton) {
                border = EmptyBorder(0, 4, 0, 4)
                isContentAreaFilled = false

                val mapHash = dmm.hashCode()

                addActionListener {
                    isMiscEvent = true
                    Environment.closeMap(dmm)
                    mapTabs.remove(indexHashList.indexOf(mapHash))
                    indexHashList.remove(mapHash)
                }
            }

            add(label)
            add(closeButton)

            toggleBoldFont(true)
        }

        fun toggleBoldFont(enable: Boolean) {
            label.font = if (enable) boldFont else plainFont
        }

        fun toggleModifiedMark(isModified: Boolean) {
            if (isModified && !hasModifiedMark) {
                hasModifiedMark = true
                label.text = label.text + " *"
            } else if (!isModified && hasModifiedMark) {
                hasModifiedMark = false
                label.text = label.text.substring(0..(label.text.length - 3))
            }
        }
    }
}
