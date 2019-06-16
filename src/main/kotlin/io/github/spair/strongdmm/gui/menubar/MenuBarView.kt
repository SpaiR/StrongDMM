package io.github.spair.strongdmm.gui.menubar

import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.gui.chooseFileDialog
import io.github.spair.strongdmm.gui.edit.LayersFilter
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.gui.map.select.SelectType
import io.github.spair.strongdmm.gui.runWithProgressBar
import io.github.spair.strongdmm.gui.showAvailableMapsDialog
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.TYPE_AREA
import io.github.spair.strongdmm.logic.dme.TYPE_MOB
import io.github.spair.strongdmm.logic.dme.TYPE_OBJ
import io.github.spair.strongdmm.logic.dme.TYPE_TURF
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.map.LayersManager
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
    private val addSelectModeItem = createRadioButton("Add Select Mode", true).addAltShortcut('1')
    private val fillSelectModeItem = createRadioButton("Fill Select Mode").addAltShortcut('2')
    private val pickSelectModeItem = createRadioButton("Pick Select Mode").addAltShortcut('3')

    // Options
    private val synchronizMaps = createRadioButton("Synchronize Maps")

    // Layers items
    private val layersFilterActionItem = createButton("Layers Filter", false)
    private val toggleAreaActionItem = createRadioButton("Area", true).addCtrlShortcut('1')
    private val toggleTurfActionItem = createRadioButton("Turf", true).addCtrlShortcut('2')
    private val toggleObjActionItem = createRadioButton("Obj", true).addCtrlShortcut('3')
    private val toggleMobActionItem = createRadioButton("Mob", true).addCtrlShortcut('4')

    override fun initComponent(): JMenuBar {
        return JMenuBar().apply {
            add(createMenu("File", createFileItems()))
            add(createMenu("Edit", createEditItems()))
            add(createMenu("Options", createOptionsItems()))
            add(createMenu("Layers", createLayersItems()))

            initLogic()
        }
    }

    private fun initLogic() {
        // File
        openEnvItem.addActionListener(openEnvironmentAction())
        openMapItem.addActionListener(openMapAction())
        availableMapsItem.addActionListener(openMapFromAvailableAction())
        saveItem.addActionListener(saveSelectedMapAction())
        exitMenuItem.addActionListener { System.exit(0) }

        // Options
        synchronizMaps.addActionListener { MapView.switchMapsSync() }

        // Edit
        undoActionItem.addActionListener { History.undoAction() }
        redoActionItem.addActionListener { History.redoAction() }
        ButtonGroup().run {
            add(addSelectModeItem.apply { addActionListener { SelectOperation.switchSelectType(SelectType.ADD) } })
            add(fillSelectModeItem.apply { addActionListener { SelectOperation.switchSelectType(SelectType.FILL) } })
            add(pickSelectModeItem.apply { addActionListener { SelectOperation.switchSelectType(SelectType.PICK) } })
        }

        // Layers
        layersFilterActionItem.addActionListener { LayersFilter().open() }
        toggleAreaActionItem.addActionListener { LayersManager.toggleType(TYPE_AREA) }
        toggleTurfActionItem.addActionListener { LayersManager.toggleType(TYPE_TURF) }
        toggleObjActionItem.addActionListener { LayersManager.toggleType(TYPE_OBJ) }
        toggleMobActionItem.addActionListener { LayersManager.toggleType(TYPE_MOB) }
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

    private fun createOptionsItems() = arrayOf<JComponent>(
        synchronizMaps
    )

    private fun createLayersItems() = arrayOf<JComponent>(
        layersFilterActionItem,
        JSeparator(),
        toggleAreaActionItem,
        toggleTurfActionItem,
        toggleObjActionItem,
        toggleMobActionItem
    )

    fun updateUndoable() {
        undoActionItem.isEnabled = History.hasUndoActions()
        redoActionItem.isEnabled = History.hasRedoActions()
    }

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
                // File
                Shortcut.CTRL_O -> openMapItem
                Shortcut.CTRL_S -> saveItem
                Shortcut.CTRL_Q -> exitMenuItem
                Shortcut.CTRL_Z -> undoActionItem
                // Edit
                Shortcut.CTRL_SHIFT_O -> availableMapsItem
                Shortcut.CTRL_SHIFT_Z -> redoActionItem
                Shortcut.ALT_1 -> addSelectModeItem
                Shortcut.ALT_2 -> fillSelectModeItem
                Shortcut.ALT_3 -> pickSelectModeItem
                // Layers
                Shortcut.CTRL_1 -> toggleAreaActionItem
                Shortcut.CTRL_2 -> toggleTurfActionItem
                Shortcut.CTRL_3 -> toggleObjActionItem
                Shortcut.CTRL_4 -> toggleMobActionItem
            }.doClick()
        }
    }

    fun switchSelectType(selectType: SelectType) {
        when (selectType) {
            SelectType.ADD -> addSelectModeItem
            SelectType.FILL -> fillSelectModeItem
            SelectType.PICK -> pickSelectModeItem
        }.isSelected = true
    }

    fun switchBasicLayers(type: String, isSelected: Boolean) {
        when (type) {
            TYPE_AREA -> toggleAreaActionItem
            TYPE_TURF -> toggleTurfActionItem
            TYPE_OBJ -> toggleObjActionItem
            TYPE_MOB -> toggleMobActionItem
            else -> null
        }?.isSelected = isSelected
    }

    private fun openEnvironmentAction() = ActionListener {
        chooseFileDialog("BYOND Environments (*.dme)", "dme")?.let { dmeFile ->
            runWithProgressBar("Parsing environment...") {
                Environment.parseAndPrepareEnv(dmeFile)
                arrayOf(saveItem, openMapItem, availableMapsItem, layersFilterActionItem).forEach {
                    it.isEnabled = true
                }
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
        MapView.getSelectedDmm()?.let { saveMap(it) }
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

    private fun JMenuItem.addAltShortcut(char: Char) = apply {
        accelerator = KeyStroke.getKeyStroke(char.toInt(), InputEvent.ALT_DOWN_MASK)
    }
}
