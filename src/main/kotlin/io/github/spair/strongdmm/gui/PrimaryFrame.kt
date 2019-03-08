package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.gui.controller.Controller
import io.github.spair.strongdmm.gui.view.LeftScreenView
import io.github.spair.strongdmm.gui.view.MenuBarView
import io.github.spair.strongdmm.gui.view.RightScreenView
import io.github.spair.strongdmm.kodein
import org.kodein.di.direct
import org.kodein.di.erased.allInstances
import org.kodein.di.erased.instance
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JSplitPane
import javax.swing.SwingConstants
import javax.swing.UIManager

class PrimaryFrame(private val windowFrame: JFrame = JFrame()) {

    private val menuBarView by kodein.instance<MenuBarView>()
    private val leftScreenView by kodein.instance<LeftScreenView>()
    private val rightScreenView by kodein.instance<RightScreenView>()

    fun init() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        initSubViews()
        initControllers()
        with(windowFrame) {
            title = "StrongDMM"
            size = Dimension(1024, 768)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setLocationRelativeTo(null)
            isVisible = true
        }
    }

    // Some subviews can have it's own subviews
    private fun initSubViews() {
        windowFrame.jMenuBar = menuBarView.init()
        windowFrame.add(
            JSplitPane(
                SwingConstants.VERTICAL, leftScreenView.init(), rightScreenView.init()
            )
        )
    }

    // Controllers should be initialized after views
    private fun initControllers() {
        kodein.direct.allInstances<Controller>().forEach(Controller::init)
    }
}
