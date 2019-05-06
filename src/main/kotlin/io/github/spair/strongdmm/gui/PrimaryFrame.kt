package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.diDirectAll
import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.menubar.MenuBarView
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

        initViews()
        initLogic()

        title = "StrongDMM"
        size = Dimension(1280, 768)
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true
    }

    // Some subviews can have it's own subviews
    private fun initViews() {
        jMenuBar = menuBarView.initComponent()
        add(tabbedObjectPanelView.initComponent(), BorderLayout.WEST)
        add(tabbedMapPanelView.initComponent(), BorderLayout.CENTER)
    }

    private fun initLogic() {
        diDirectAll<View>().forEach(View::initLogic)
    }
}
