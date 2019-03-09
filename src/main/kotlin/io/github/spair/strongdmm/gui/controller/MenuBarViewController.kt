package io.github.spair.strongdmm.gui.controller

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.util.chooseFileDialog
import io.github.spair.strongdmm.gui.util.runWithProgressBar
import io.github.spair.strongdmm.gui.util.showAvailableMapsDialog
import io.github.spair.strongdmm.gui.view.MenuBarView
import io.github.spair.strongdmm.logic.Environment
import org.kodein.di.direct
import org.kodein.di.erased.instance
import java.awt.event.ActionListener

class MenuBarViewController : ViewController<MenuBarView>(DI.direct.instance()) {

    private val environment by DI.instance<Environment>()

    override fun init() {
        view.openEnvItem.addActionListener(openEnvironmentAction())
        view.exitMenuItem.addActionListener { System.exit(0) }
    }

    private fun openEnvironmentAction() = ActionListener {
        chooseFileDialog("BYOND Environments (*.dme)", "dme")?.let { dmeFile ->
            runWithProgressBar("Parsing environment...") {
                environment.parseAndPrepareEnv(dmeFile)
                view.availableMapsItem.apply {
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
