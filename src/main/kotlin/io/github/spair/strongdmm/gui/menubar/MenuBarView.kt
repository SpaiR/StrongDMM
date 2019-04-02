package io.github.spair.strongdmm.gui.menubar

import io.github.spair.strongdmm.gui.View
import java.awt.Font
import java.awt.event.InputEvent
import javax.swing.*

class MenuBarView : View {

    // File items
    val openEnvItem = createMenuItem("Open Environment...")
    val openMapItem = createMenuItem("Open...", false).addCtrlShortcut('O')
    val availableMapsItem = createMenuItem("Open from available", false).addCtrlShiftShortcut('O')
    val saveItem = createMenuItem("Save", false).addCtrlShortcut('S')
    val exitMenuItem = createMenuItem("Exit").addCtrlShortcut('Q')

    // Edit items
    val undoActionItem = createMenuItem("Undo", false).addCtrlShortcut('Z')
    val redoActionItem = createMenuItem("Redo", false).addCtrlShiftShortcut('Z')

    override fun init(): JMenuBar {
        return JMenuBar().apply {
            add(createMenu("File", createFileItems()))
            add(createMenu("Edit", createEditItems()))
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
        redoActionItem
    )

    private fun createMenuItem(text: String, isEnabled: Boolean = true): JMenuItem {
        return JMenuItem(text).apply {
            font = font.deriveFont(Font.PLAIN)
            this.isEnabled = isEnabled
        }
    }

    private fun createMenu(name: String, items: Array<JComponent>): JComponent = JMenu(name).apply {
        items.forEach { add(it) }
    }

    private fun JMenuItem.addCtrlShortcut(char: Char) = apply {
        accelerator = KeyStroke.getKeyStroke(char.toInt(), InputEvent.CTRL_DOWN_MASK)
    }

    private fun JMenuItem.addCtrlShiftShortcut(char: Char) = apply {
        accelerator = KeyStroke.getKeyStroke(char.toInt(), InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK)
    }
}
