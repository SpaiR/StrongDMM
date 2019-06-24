package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.gui.menubar.MenuBarView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Insets
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.UIManager

object PrimaryFrame : JFrame() {

    fun init() {
        initUI()
        initViews()
        title = "StrongDMM"
        size = Dimension(1280, 768)
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
    }

    private fun initUI() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        UIManager.getDefaults()["TabbedPane.contentBorderInsets"] = Insets(0, 0, 0, 0)
        UIManager.getDefaults()["TabbedPane.tabAreaInsets"] = Insets(0, 0, 0, 0)
        iconImage = ImageIO.read(PrimaryFrame::class.java.classLoader.getResource("icon.png"))
    }

    // Views have it's own subviews
    private fun initViews() {
        jMenuBar = MenuBarView.initComponent()
        add(TabbedObjectPanelView.initComponent(), BorderLayout.WEST)
        add(TabbedMapPanelView.initComponent(), BorderLayout.CENTER)
    }
}
