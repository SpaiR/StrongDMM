package io.github.spair.strongdmm.gui

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.common.CenterScreenView
import io.github.spair.strongdmm.gui.common.Controller
import io.github.spair.strongdmm.gui.common.LeftScreenView
import io.github.spair.strongdmm.gui.menubar.MenuBarView
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
    private val centerScreenView by DI.instance<CenterScreenView>()

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
        DI.direct.allInstances<Controller>().forEach(Controller::init)
    }
}
