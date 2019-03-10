package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.controller.Controller
import io.github.spair.strongdmm.gui.view.LeftScreenView
import io.github.spair.strongdmm.gui.view.MenuBarView
import io.github.spair.strongdmm.gui.view.RightScreenView
import org.kodein.di.direct
import org.kodein.di.erased.allInstances
import org.kodein.di.erased.instance
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.UIManager

class PrimaryFrame(val windowFrame: JFrame = JFrame()) {

    private val menuBarView by DI.instance<MenuBarView>()
    private val leftScreenView by DI.instance<LeftScreenView>()
    private val rightScreenView by DI.instance<RightScreenView>()

    fun init() {
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
        windowFrame.add(BorderLayout.WEST, leftScreenView.init())
        windowFrame.add(BorderLayout.EAST, rightScreenView.init())
    }

    // Controllers should be initialized after views
    private fun initControllers() {
        DI.direct.allInstances<Controller>().forEach(Controller::init)
    }
}
