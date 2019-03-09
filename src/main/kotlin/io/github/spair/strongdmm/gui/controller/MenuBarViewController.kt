package io.github.spair.strongdmm.gui.controller

import io.github.spair.strongdmm.gui.util.chooseFileDialog
import io.github.spair.strongdmm.gui.util.runWithProgressBar
import io.github.spair.strongdmm.gui.util.showAvailableMapsDialog
import io.github.spair.strongdmm.gui.view.MenuBarView
import io.github.spair.strongdmm.kodein
import io.github.spair.strongdmm.logic.Environment
import org.kodein.di.erased.instance
import java.awt.event.ActionListener

class MenuBarViewController : Controller {

    private val menuBarView by kodein.instance<MenuBarView>()
    private val environment by kodein.instance<Environment>()

    override fun init() {
        menuBarView.openEnvItem.addActionListener(openEnvironmentAction())
        menuBarView.exitMenuItem.addActionListener { System.exit(0) }
    }

    private fun openEnvironmentAction() = ActionListener {
        chooseFileDialog("BYOND Environments (*.dme)", "dme")?.let { dmeFile ->
            runWithProgressBar("Parsing environment...") {
                environment.parseAndPrepareEnv(dmeFile)
                menuBarView.availableMapsItem.apply {
                    addActionListener(openAvailableMapsAction(environment.availableMaps))
                    isEnabled = true
                }
            }
        }
    }

    private fun openAvailableMapsAction(availableMaps: List<String>) = ActionListener {
        showAvailableMapsDialog(availableMaps)?.let {
            println(it)
        }
    }
}
