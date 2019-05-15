package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.gui.menubar.MenuBarView
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.UIManager

object PrimaryFrame : JFrame() {

    fun init() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        initViews()

        title = "StrongDMM"
        size = Dimension(1280, 768)
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
    }

    // Views have it's own subviews
    private fun initViews() {
        jMenuBar = MenuBarView.initComponent()
        add(TabbedObjectPanelView.initComponent(), BorderLayout.WEST)
        add(TabbedMapPanelView.initComponent(), BorderLayout.CENTER)
    }
}
