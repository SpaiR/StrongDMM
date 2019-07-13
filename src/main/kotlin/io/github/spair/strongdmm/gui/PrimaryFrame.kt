package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.gui.common.Dialog
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.map.save.SaveMap
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Insets
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.UIManager
import kotlin.system.exitProcess

object PrimaryFrame : JFrame() {

    fun init() {
        initUI()
        initViews()
        title = "StrongDMM"
        size = Dimension(1280, 768)
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
    }

    fun handleWindowClosing() {
        for (map in MapView.getOpenedMaps()) {
            if (ActionController.hasChanges(map)) {
                val answer = Dialog.askToSaveMap(map.mapName)

                if (answer == Dialog.MAP_SAVE_YES) {
                    ActionController.resetActionBalance(map)
                    block()
                    SaveMap(map)
                    unblock()
                } else if (answer == Dialog.MAP_SAVE_NO) {
                    continue
                } else if (answer == Dialog.MAP_SAVE_CANCEL) {
                    return
                }
            }
        }

        exitProcess(0)
    }

    fun block() {
        isEnabled = false
    }

    fun unblock() {
        isEnabled = true
    }

    private fun initUI() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        UIManager.getDefaults()["TabbedPane.contentBorderInsets"] = Insets(0, 0, 0, 0)
        UIManager.getDefaults()["TabbedPane.tabAreaInsets"] = Insets(0, 0, 0, 0)
        iconImage = ImageIO.read(PrimaryFrame::class.java.classLoader.getResource("icon.png"))

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                handleWindowClosing()
            }
        })
    }

    // Views have it's own subviews
    private fun initViews() {
        jMenuBar = MenuBarView.initComponent()
        add(TabbedObjectPanelView.initComponent(), BorderLayout.WEST)
        add(TabbedMapPanelView.initComponent(), BorderLayout.CENTER)
    }
}
