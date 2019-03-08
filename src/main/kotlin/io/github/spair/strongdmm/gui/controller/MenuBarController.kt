package io.github.spair.strongdmm.gui.controller

import io.github.spair.strongdmm.gui.util.chooseFileDialog
import io.github.spair.strongdmm.kodein
import io.github.spair.strongdmm.gui.view.MenuBarView
import org.kodein.di.erased.instance
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class MenuBarController : Controller {

    private val menuBarView by kodein.instance<MenuBarView>()

    override fun init() {
        menuBarView.openEnvItem.addActionListener(OpenEnvironmentAction())
        menuBarView.exitMenuItem.addActionListener { System.exit(0) }
    }

    private class OpenEnvironmentAction : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            println(chooseFileDialog("BYOND Environments (*.dme)", "dme"))
        }
    }
}
