package io.github.spair.strongdmm.gui.menubar

import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.gui.chooseFileDialog
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.gui.map.select.SelectType
import io.github.spair.strongdmm.gui.runWithProgressBar
import io.github.spair.strongdmm.gui.showAvailableMapsDialog
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.map.saveMap
import java.awt.event.ActionListener
import java.awt.event.InputEvent
import javax.swing.*

object MenuBarView : View {

    // File items
    private val openEnvItem = createButton("Open Environment...")
    private val openMapItem = createButton("Open...", false).addCtrlShortcut('O')
    private val availableMapsItem = createButton("Open from available", false).addCtrlShiftShortcut('O')
    private val saveItem = createButton("Save", false).addCtrlShortcut('S')
    private val exitMenuItem = createButton("Exit").addCtrlShortcut('Q')

    // Edit items
    private val undoActionItem = createButton("Undo", false).addCtrlShortcut('Z')
    private val redoActionItem = createButton("Redo", false).addCtrlShiftShortcut('Z')
    private val addSelectModeItem = createRadioButton("Add Select Mode", true)
    private val fillSelectModeItem = createRadioButton("Fill Select Mode")
    private val pickSelectModeItem = createRadioButton("Pick Select Mode")

    override fun initComponent(): JMenuBar {
        return JMenuBar().apply {
            add(createMenu("File", createFileItems()))
            add(createMenu("Edit", createEditItems()))

            initLogic()
        }
    }

    private fun initLogic() {
        openEnvItem.addActionListener(openEnvironmentAction())
        openMapItem.addActionListener(openMapAction())
        availableMapsItem.addActionListener(openMapFromAvailableAction())
        saveItem.addActionListener(saveSelectedMapAction())
        exitMenuItem.addActionListener { System.exit(0) }

        undoActionItem.addActionListener { History.undoAction() }
        redoActionItem.addActionListener { History.redoAction() }
        ButtonGroup().run {
            add(addSelectModeItem.apply {
                addActionListener {
                    SelectOperation.switchSelectMode(SelectType.ADD)
                }
            })
            add(fillSelectModeItem.apply {
                addActionListener {
                    SelectOperation.switchSelectMode(SelectType.FILL)
                }
            })
            add(pickSelectModeItem.apply {
                addActionListener {
                    SelectOperation.switchSelectMode(SelectType.PICK)
                }
            })
        }
    }

    private fun createFileItems() = arrayOf<JComponent>(
        openEnvItem,
        JSeparator(),
        openMapItem,
        availableMapsItem,
        JSeparator(),
        saveItem,
        JSeparator(),
        exitMenuItem
    )

    private fun createEditItems() = arrayOf<JComponent>(
        undoActionItem,
        redoActionItem,
        JSeparator(),
        addSelectModeItem,
        fillSelectModeItem,
        pickSelectModeItem
    )

    fun switchUndo(enabled: Boolean) {
        undoActionItem.isEnabled = enabled
    }

    fun switchRedo(enabled: Boolean) {
        redoActionItem.isEnabled = enabled
    }

    // While map canvas is in focus, Swing won't catch key events, so we fire them programmatically
    fun fireShortcutEvent(shortcut: Shortcut) {
        SwingUtilities.invokeLater {
            when (shortcut) {
                Shortcut.CTRL_O -> openMapItem
                Shortcut.CTRL_S -> saveItem
                Shortcut.CTRL_Q -> exitMenuItem
                Shortcut.CTRL_Z -> undoActionItem
                Shortcut.CTRL_SHIFT_O -> availableMapsItem
                Shortcut.CTRL_SHIFT_Z -> redoActionItem
            }.doClick()
        }
    }

    private fun openEnvironmentAction() = ActionListener {
        chooseFileDialog("BYOND Environments (*.dme)", "dme")?.let { dmeFile ->
            runWithProgressBar("Parsing environment...") {
                Environment.parseAndPrepareEnv(dmeFile)
                saveItem.isEnabled = true
                openMapItem.isEnabled = true
                availableMapsItem.isEnabled = true
            }
        }
    }

    private fun openMapAction() = ActionListener {
        chooseFileDialog("BYOND Maps (*.dmm)", "dmm", Environment.absoluteRootPath)?.let { dmmFile ->
            Environment.openMap(dmmFile)
        }
    }

    private fun openMapFromAvailableAction() = ActionListener {
        showAvailableMapsDialog(Environment.availableMaps)?.let {
            Environment.openMap(it)
        }
    }

    private fun saveSelectedMapAction() = ActionListener {
        MapView.getSelectedMap()?.let { saveMap(it) }
    }

    private fun createButton(text: String, isEnabled: Boolean = true): JMenuItem {
        return JMenuItem(text).apply {
            this.isEnabled = isEnabled
        }
    }

    private fun createRadioButton(text: String, isSelected: Boolean = false): JRadioButtonMenuItem {
        return JRadioButtonMenuItem(text).apply {
            this.isSelected = isSelected
        }
    }

    private fun createMenu(name: String, items: Array<JComponent>) = JMenu(name).apply {
        items.forEach { add(it) }
    }

    private fun JMenuItem.addCtrlShortcut(char: Char) = apply {
        accelerator = KeyStroke.getKeyStroke(char.toInt(), InputEvent.CTRL_DOWN_MASK)
    }

    private fun JMenuItem.addCtrlShiftShortcut(char: Char) = apply {
        accelerator = KeyStroke.getKeyStroke(char.toInt(), InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK)
    }
}
