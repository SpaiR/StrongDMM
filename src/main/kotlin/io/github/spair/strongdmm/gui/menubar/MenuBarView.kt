package io.github.spair.strongdmm.gui.menubar

import io.github.spair.strongdmm.common.TYPE_AREA
import io.github.spair.strongdmm.common.TYPE_MOB
import io.github.spair.strongdmm.common.TYPE_OBJ
import io.github.spair.strongdmm.common.TYPE_TURF
import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.TabbedMapPanelView
import io.github.spair.strongdmm.gui.common.BorderUtil
import io.github.spair.strongdmm.gui.common.Dialog
import io.github.spair.strongdmm.gui.common.View
import io.github.spair.strongdmm.gui.edit.LayersFilter
import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.map.ModOperation
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.gui.map.select.SelectType
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.Workspace
import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.map.LayersManager
import io.github.spair.strongdmm.logic.map.MapManager
import io.github.spair.strongdmm.logic.map.save.SaveMap
import java.awt.event.ActionListener
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.*

object MenuBarView : View {

    // File
    private val openEnvBtn = createButton("Open Environment...")
    private val recentEnvMenu = createMenu("Recent Environments")
    private val newMapBtn = createButton("New Map...", false).addCtrlShortcut('N')
    private val openMapBtn = createButton("Open Map...", false).addCtrlShortcut('O')
    private val availableMapsBtn = createButton("Open Available Map", false).addCtrlShiftShortcut('O')
    private val recentMapsMenu = createMenu("Recent Maps", isEnabled = false)
    private val saveBtn = createButton("Save", false).addCtrlShortcut('S')
    private val saveAllBtn = createButton("Save All", false).addCtrlShiftShortcut('S')
    private val closeBtn = createButton("Close", false).addCtrlShortcut('W')
    private val closeAllBtn = createButton("Close All", false).addCtrlShiftShortcut('W')
    private val exitMenuBtn = createButton("Exit").addCtrlShortcut('Q')

    // Edit
    private val undoActionBtn = createButton("Undo", false).addCtrlShortcut('Z')
    private val redoActionBtn = createButton("Redo", false).addCtrlShiftShortcut('Z')
    private val cutBtn = createButton("Cut").addCtrlShortcut('X')
    private val copyBtn = createButton("Copy").addCtrlShortcut('C')
    private val pasteBtn = createButton("Paste").addCtrlShortcut('V')
    private val deleteBtn = createButton("Delete").addPlainShortcut(KeyEvent.VK_DELETE)
    private val deselectBtn = createButton("Deselect").addPlainShortcut(KeyEvent.VK_ESCAPE)
    private val addSelectModeOpt = createRadioButton("Add Select Mode", true).addAltShortcut('1')
    private val fillSelectModeOpt = createRadioButton("Fill Select Mode").addAltShortcut('2')
    private val pickSelectModeOpt = createRadioButton("Pick Select Mode").addAltShortcut('3')
    // Edit -- Save Mode (sub menu)
    private val tgmSaveModeOpt = createRadioButton("TGM", Workspace.isTgmSaveMode())
    private val byondSaveModeOpt = createRadioButton("BYOND", !Workspace.isTgmSaveMode())

    // Options
    private val setMapSizeBtn = createButton("Set Map Size...")
    private val nextMapBtn = createButton("Next Map").addCtrlShortcut(KeyEvent.VK_RIGHT)
    private val prevMapBtn = createButton("Prev Map").addCtrlShortcut(KeyEvent.VK_LEFT)
    private val frameAreasOpt = createRadioButton("Frame Areas", true)
    private val syncMapsOpt = createRadioButton("Synchronize Maps")

    // Layers
    private val layersFilterActionBtn = createButton("Layers Filter", false)
    private val toggleAreaActionOpt = createRadioButton("Area", true).addCtrlShortcut('1')
    private val toggleTurfActionOpt = createRadioButton("Turf", true).addCtrlShortcut('2')
    private val toggleObjActionOpt = createRadioButton("Obj", true).addCtrlShortcut('3')
    private val toggleMobActionOpt = createRadioButton("Mob", true).addCtrlShortcut('4')

    // Help
    private val aboutBtn = createButton("About")

    // Enabled when environment becomes available
    private val envDependentButtons = arrayOf(
        newMapBtn, saveBtn, saveAllBtn, openMapBtn, closeBtn, closeAllBtn,
        availableMapsBtn, layersFilterActionBtn, recentMapsMenu
    )

    override fun initComponent(): JMenuBar {
        return JMenuBar().apply {
            add(createMenu("File", createFileItems()))
            add(createMenu("Edit", createEditItems()))
            add(createMenu("Options", createOptionsItems()))
            add(createMenu("Layers", createLayersItems()))
            add(createMenu("Help", createHelpItems()))
            updateRecentEnvironments()
            initLogic()
        }
    }

    private fun initLogic() {
        // File
        newMapBtn.addActionListener(createNewMapAction())
        openEnvBtn.addActionListener(createOpenEnvironmentAction())
        openMapBtn.addActionListener(createOpenMapAction())
        availableMapsBtn.addActionListener(createOpenMapFromAvailableAction())
        saveBtn.addActionListener(createSaveSelectedMapAction())
        saveAllBtn.addActionListener(createSaveAllMapsAction())
        closeBtn.addActionListener(createCloseMapAction())
        closeAllBtn.addActionListener(createCloseAllMapsAction())
        exitMenuBtn.addActionListener { PrimaryFrame.handleWindowClosing() }

        // Options
        setMapSizeBtn.addActionListener(createSetMapSizeAction())
        nextMapBtn.addActionListener { TabbedMapPanelView.selectNextMap() }
        prevMapBtn.addActionListener { TabbedMapPanelView.selectPrevMap() }
        frameAreasOpt.addActionListener { MapView.switchAreasFraming() }
        syncMapsOpt.addActionListener { MapView.switchMapsSync() }

        // Edit
        undoActionBtn.addActionListener { ActionController.undoAction() }
        redoActionBtn.addActionListener { ActionController.redoAction() }
        cutBtn.addActionListener { ModOperation.cut(MapView.getSelectedDmm(), MapView.getMouseTileX(), MapView.getMouseTileY()) }
        copyBtn.addActionListener { ModOperation.copy(MapView.getSelectedDmm(), MapView.getMouseTileX(), MapView.getMouseTileY()) }
        pasteBtn.addActionListener { ModOperation.paste(MapView.getSelectedDmm(), MapView.getMouseTileX(), MapView.getMouseTileY()) }
        deleteBtn.addActionListener { ModOperation.delete(MapView.getSelectedDmm(), MapView.getMouseTileX(), MapView.getMouseTileY()) }
        deselectBtn.addActionListener { SelectOperation.depickArea() }
        ButtonGroup().run {
            add(addSelectModeOpt.apply { addActionListener { SelectOperation.switchSelectType(SelectType.ADD) } })
            add(fillSelectModeOpt.apply { addActionListener { SelectOperation.switchSelectType(SelectType.FILL) } })
            add(pickSelectModeOpt.apply { addActionListener { SelectOperation.switchSelectType(SelectType.PICK) } })
        }
        ButtonGroup().run {
            add(tgmSaveModeOpt.apply { addActionListener { Workspace.setTgmSaveMode(true) } })
            add(byondSaveModeOpt.apply { addActionListener { Workspace.setTgmSaveMode(false) } })
        }

        // Layers
        layersFilterActionBtn.addActionListener { LayersFilter().open() }
        toggleAreaActionOpt.addActionListener { LayersManager.toggleType(TYPE_AREA) }
        toggleTurfActionOpt.addActionListener { LayersManager.toggleType(TYPE_TURF) }
        toggleObjActionOpt.addActionListener { LayersManager.toggleType(TYPE_OBJ) }
        toggleMobActionOpt.addActionListener { LayersManager.toggleType(TYPE_MOB) }

        // Help
        aboutBtn.addActionListener { Dialog.showHtmlContent("StrongDMM", "about.html", 400, 300) }
    }

    private fun createFileItems() = arrayOf<JComponent>(
        openEnvBtn,
        recentEnvMenu,
        JSeparator(),
        newMapBtn,
        openMapBtn,
        availableMapsBtn,
        recentMapsMenu,
        JSeparator(),
        saveBtn,
        saveAllBtn,
        JSeparator(),
        closeBtn,
        closeAllBtn,
        JSeparator(),
        exitMenuBtn
    )

    private fun createEditItems() = arrayOf<JComponent>(
        undoActionBtn,
        redoActionBtn,
        JSeparator(),
        cutBtn,
        copyBtn,
        pasteBtn,
        deleteBtn,
        deselectBtn,
        JSeparator(),
        addSelectModeOpt,
        fillSelectModeOpt,
        pickSelectModeOpt,
        JSeparator(),
        createMenu("Save Mode", arrayOf(tgmSaveModeOpt, byondSaveModeOpt))
    )

    private fun createOptionsItems() = arrayOf<JComponent>(
        setMapSizeBtn,
        JSeparator(),
        nextMapBtn,
        prevMapBtn,
        JSeparator(),
        syncMapsOpt,
        frameAreasOpt
    )

    private fun createLayersItems() = arrayOf<JComponent>(
        layersFilterActionBtn,
        JSeparator(),
        toggleAreaActionOpt,
        toggleTurfActionOpt,
        toggleObjActionOpt,
        toggleMobActionOpt
    )

    private fun createHelpItems() = arrayOf<JComponent>(
        aboutBtn
    )

    fun updateUndoable() {
        undoActionBtn.isEnabled = ActionController.hasUndoActions()
        redoActionBtn.isEnabled = ActionController.hasRedoActions()
    }

    fun switchUndo(enabled: Boolean) {
        undoActionBtn.isEnabled = enabled
    }

    fun switchRedo(enabled: Boolean) {
        redoActionBtn.isEnabled = enabled
    }

    // While map canvas is in focus, Swing won't catch key events, so we fire them programmatically
    fun fireShortcutEvent(shortcut: Shortcut) {
        SwingUtilities.invokeLater {
            when (shortcut) {
                // File
                Shortcut.CTRL_N -> newMapBtn
                Shortcut.CTRL_O -> openMapBtn
                Shortcut.CTRL_S -> saveBtn
                Shortcut.CTRL_SHIFT_S -> saveAllBtn
                Shortcut.CTRL_W -> closeBtn
                Shortcut.CTRL_SHIFT_W -> closeAllBtn
                Shortcut.CTRL_Q -> exitMenuBtn
                // Options
                Shortcut.CTRL_LEFT_ARROW -> prevMapBtn
                Shortcut.CTRL_RIGHT_ARROW -> nextMapBtn
                // Edit
                Shortcut.CTRL_SHIFT_O -> availableMapsBtn
                Shortcut.CTRL_Z -> undoActionBtn
                Shortcut.CTRL_SHIFT_Z -> redoActionBtn
                Shortcut.CTRL_X -> cutBtn
                Shortcut.CTRL_C -> copyBtn
                Shortcut.CTRL_V -> pasteBtn
                Shortcut.DELETE -> deleteBtn
                Shortcut.ESCAPE -> deselectBtn
                Shortcut.ALT_1 -> addSelectModeOpt
                Shortcut.ALT_2 -> fillSelectModeOpt
                Shortcut.ALT_3 -> pickSelectModeOpt
                // Layers
                Shortcut.CTRL_1 -> toggleAreaActionOpt
                Shortcut.CTRL_2 -> toggleTurfActionOpt
                Shortcut.CTRL_3 -> toggleObjActionOpt
                Shortcut.CTRL_4 -> toggleMobActionOpt
            }.doClick()
        }
    }

    fun switchSelectType(selectType: SelectType) {
        when (selectType) {
            SelectType.ADD -> addSelectModeOpt
            SelectType.FILL -> fillSelectModeOpt
            SelectType.PICK -> pickSelectModeOpt
        }.isSelected = true
    }

    fun switchBasicLayers(type: String, isSelected: Boolean) {
        when (type) {
            TYPE_AREA -> toggleAreaActionOpt
            TYPE_TURF -> toggleTurfActionOpt
            TYPE_OBJ -> toggleObjActionOpt
            TYPE_MOB -> toggleMobActionOpt
            else -> null
        }?.isSelected = isSelected
    }

    fun updateRecentEnvironments() {
        recentEnvMenu.removeAll()

        Workspace.getRecentEnvironmentsPaths().forEach { dmeFilePath ->
            val openButton = createButton(dmeFilePath)

            openButton.addActionListener {
                val file = File(dmeFilePath)
                if (file.exists()) {
                    openEnvironment(dmeFilePath)
                } else {
                    recentEnvMenu.remove(openButton)
                    Workspace.removeRecentEnvironment(dmeFilePath)
                }
            }

            recentEnvMenu.add(openButton)
        }
    }

    fun updateRecentMaps() {
        recentMapsMenu.removeAll()

        val envPath = Environment.dme.path

        Workspace.getRecentMapsPaths(envPath).forEach { dmmFilePath ->
            val openButton = createButton(dmmFilePath)

            openButton.addActionListener {
                val file = File(dmmFilePath)
                if (file.exists()) {
                    Environment.openMap(file)
                } else {
                    recentMapsMenu.remove(openButton)
                    Workspace.removeRecentMap(envPath, dmmFilePath)
                }
            }

            recentMapsMenu.add(openButton)
        }
    }

    private fun createOpenEnvironmentAction() = ActionListener {
        Dialog.chooseFile("BYOND Environments (*.dme)", "dme")?.let {
            openEnvironment(it.path)
        }
    }

    private fun createOpenMapAction() = ActionListener {
        Dialog.chooseFile("BYOND Maps (*.dmm)", "dmm", Environment.absoluteRootPath)?.let {
            openMap(it.path)
        }
    }

    private fun createOpenMapFromAvailableAction() = ActionListener {
        val dmmList = JList(Environment.availableMaps.toTypedArray())
        dmmList.border = BorderUtil.createEmptyBorder(5)

        val dialogPane = JScrollPane(dmmList)
        val result = JOptionPane.showConfirmDialog(PrimaryFrame, dialogPane, "Select map to open", JOptionPane.OK_CANCEL_OPTION)

        if (result != JOptionPane.CANCEL_OPTION) {
            openMap(dmmList.selectedValue)
        }
    }

    private fun createSaveSelectedMapAction() = ActionListener {
        MapView.getSelectedDmm()?.let {
            ActionController.resetActionBalance(it)
            PrimaryFrame.block()
            SaveMap(it)
            PrimaryFrame.unblock()
        }
    }

    private fun createSaveAllMapsAction() = ActionListener {
        MapView.getOpenedMaps().forEach {
            ActionController.resetActionBalance(it)
            PrimaryFrame.block()
            SaveMap(it)
            PrimaryFrame.unblock()
        }
    }

    private fun createCloseMapAction() = ActionListener {
        MapView.getSelectedDmm()?.let {
            TabbedMapPanelView.closeMap(it)
        }
    }

    private fun createCloseAllMapsAction() = ActionListener {
        MapView.getOpenedMaps().forEach {
            TabbedMapPanelView.closeMap(it)
        }
    }

    private fun openEnvironment(dmeFilePath: String) {
        Dialog.runWithProgressBar("Parsing environment...") {
            Environment.openEnv(dmeFilePath)
            envDependentButtons.forEach { it.isEnabled = true }
        }
    }

    private fun openMap(dmmFilePath: String) {
        Environment.openMap(dmmFilePath)
    }

    private fun createSetMapSizeAction() = ActionListener {
        MapView.getSelectedDmm()?.let { dmm ->
            Dialog.askMapSize(dmm.getMaxX(), dmm.getMaxY())?.let { (x, y) ->
                if (x != dmm.getMaxX() || dmm.getMaxY() != y) {
                    MapManager.setMapSize(dmm, x, y)
                    Frame.update(true)
                }
            }
        }
    }

    private fun createNewMapAction() = ActionListener {
        Dialog.createFile("BYOND Map (*.dmm)", "dmm", Environment.absoluteRootPath)?.let { mapFile ->
            Dialog.askMapSize(1, 1)?.let { (initX, initY) ->
                MapManager.saveNewMap(mapFile, initX, initY)
                Environment.openMap(mapFile)
            }
        }
    }

    // Util shit below

    private fun createMenu(name: String, items: Array<JComponent>? = null, isEnabled: Boolean = true) = JMenu(name).apply {
        this.isEnabled = isEnabled
        popupMenu.isLightWeightPopupEnabled = false
        items?.forEach { add(it) }
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

    private fun JMenuItem.addPlainShortcut(char: Int) = apply {
        accelerator = KeyStroke.getKeyStroke(char, 0)
    }

    private fun JMenuItem.addCtrlShortcut(char: Char) = apply {
        accelerator = KeyStroke.getKeyStroke(char.toInt(), InputEvent.CTRL_DOWN_MASK)
    }

    private fun JMenuItem.addCtrlShortcut(char: Int) = apply {
        accelerator = KeyStroke.getKeyStroke(char, InputEvent.CTRL_DOWN_MASK)
    }

    private fun JMenuItem.addCtrlShiftShortcut(char: Char) = apply {
        accelerator = KeyStroke.getKeyStroke(char.toInt(), InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK)
    }

    private fun JMenuItem.addAltShortcut(char: Char) = apply {
        accelerator = KeyStroke.getKeyStroke(char.toInt(), InputEvent.ALT_DOWN_MASK)
    }
}
