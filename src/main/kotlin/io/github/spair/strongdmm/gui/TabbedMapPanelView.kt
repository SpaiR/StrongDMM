package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.gui.common.BorderUtil
import io.github.spair.strongdmm.gui.common.Dialog
import io.github.spair.strongdmm.gui.common.View
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.logic.EnvCleanable
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.saveMap
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*

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
            add(StatusView.initComponent(), BorderLayout.SOUTH)
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

    fun markMapModified(dmm: Dmm, isModified: Boolean) {
        getTab(dmm.mapPath)?.toggleModifiedMark(isModified)
    }

    fun selectNextMap() {
        switchMap(mapTabs.selectedIndex + 1)
    }

    fun selectPrevMap() {
        switchMap(mapTabs.selectedIndex - 1)
    }

    private fun switchMap(index: Int) {
        if (indexHashList.isNotEmpty() && index >= 0 && index <= mapTabs.tabCount - 1) {
            previousIndex = mapTabs.selectedIndex
            mapTabs.selectedIndex = index
        }
    }

    private fun getTab(index: Int): MapTab? {
        return if (index < 0 || index >= mapTabs.tabCount) null else mapTabs.getTabComponentAt(index) as? MapTab
    }

    private fun getTab(mapPath: String): MapTab? {
        for (index in 0 until mapTabs.tabCount) {
            val tab = mapTabs.getTabComponentAt(index) as MapTab
            if (tab.mapPath == mapPath) {
                return tab
            }
        }
        return null
    }

    private class MapTab(dmm: Dmm) : JPanel(FlowLayout(FlowLayout.CENTER, 4, 0)) {

        val mapPath: String = dmm.mapPath

        private val plainFont: Font = font.deriveFont(Font.PLAIN)
        private val boldFont: Font = font.deriveFont(Font.BOLD)

        private val label: JLabel = JLabel(dmm.mapName)
        private val closeButton: JButton = JButton("x")

        private var hasModifiedMark: Boolean = false

        init {
            isOpaque = false

            with(closeButton) {
                border = BorderUtil.createEmptyBorder(left = 4, right = 4)
                isContentAreaFilled = false

                val mapHash = dmm.hashCode()

                addActionListener {
                    if (ActionController.hasChanges(dmm)) {
                        val answer = Dialog.askToSaveMap(dmm.mapName)
                        if (answer == Dialog.MAP_SAVE_YES) {
                            saveMap(dmm)
                        } else if (answer == Dialog.MAP_SAVE_CANCEL) {
                            return@addActionListener
                        }
                    }

                    isMiscEvent = true
                    Environment.closeMap(dmm)
                    ActionController.resetActionBalance(dmm)
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
