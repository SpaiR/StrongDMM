package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.diDirectAll
import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.gui.tabbedmappanel.TabbedMapPanelView
import io.github.spair.strongdmm.gui.tabbedobjpanel.TabbedObjectPanelView
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.UIManager

object PrimaryFrame : JFrame() {

    private val menuBarView by diInstance<MenuBarView>()
    private val tabbedObjectPanelView by diInstance<TabbedObjectPanelView>()
    private val tabbedMapPanelView by diInstance<TabbedMapPanelView>()

    fun init() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        initSubViews()
        initControllers()

        title = "StrongDMM"
        size = Dimension(1280, 768)
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
    }

    // Some subviews can have it's own subviews
    private fun initSubViews() {
        jMenuBar = menuBarView.init()
        add(tabbedObjectPanelView.init(), BorderLayout.WEST)
        add(tabbedMapPanelView.init(), BorderLayout.CENTER)
    }

    // Controllers should be initialized after views
    private fun initControllers() {
        diDirectAll<Controller>().forEach(Controller::init)
    }
}
