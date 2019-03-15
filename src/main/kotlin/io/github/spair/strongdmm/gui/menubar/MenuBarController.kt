package io.github.spair.strongdmm.gui.menubar

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.chooseFileDialog
import io.github.spair.strongdmm.gui.common.ViewController
import io.github.spair.strongdmm.gui.runWithProgressBar
import io.github.spair.strongdmm.gui.showAvailableMapsDialog
import io.github.spair.strongdmm.logic.Environment
import org.kodein.di.direct
import org.kodein.di.erased.instance
import java.awt.event.ActionListener

class MenuBarController : ViewController<MenuBarView>(DI.direct.instance()) {

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
                    addActionListener {
                        showAvailableMapsDialog(environment.availableMaps)?.let {
                            environment.openMap(it)
                        }
                    }
                    isEnabled = true
                }
            }
        }
    }
}
