package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.diDirectAll
import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.UIManager

class PrimaryFrame(val windowFrame: JFrame = JFrame()) {

    private val menuBarView by diInstance<MenuBarView>()
    private val leftScreenView by diInstance<LeftScreenView>()
    private val centerScreenView by diInstance<CenterScreenView>()

    init {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        initSubViews()
        initControllers()

        with(windowFrame) {
            title = "StrongDMM"
            size = Dimension(1280, 768)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setLocationRelativeTo(null)
            isVisible = true
        }
    }

    // Some subviews can have it's own subviews
    private fun initSubViews() {
        windowFrame.jMenuBar = menuBarView.init()
        windowFrame.add(leftScreenView.init(), BorderLayout.WEST)
        windowFrame.add(centerScreenView.init(), BorderLayout.CENTER)
    }

    // Controllers should be initialized after views
    private fun initControllers() {
        diDirectAll<Controller>().forEach(Controller::init)
    }
}
