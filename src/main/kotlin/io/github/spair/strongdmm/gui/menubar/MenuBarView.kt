package io.github.spair.strongdmm.gui.menubar

import io.github.spair.strongdmm.gui.View
import java.awt.Font
import javax.swing.*

class MenuBarView : View {

    val openEnvItem = createMenuItem("Open Environment...")

    val openMapItem = createMenuItem("Open...", false)
    val availableMapsItem = createMenuItem("Open from available", false)

    val saveItem = createMenuItem("Save", false)

    val exitMenuItem = createMenuItem("Exit")

    override fun init(): JMenuBar {
        return JMenuBar().apply {
            add(createMenu("File", createFileItems()))
        }
    }

    private fun createFileItems(): Array<JComponent> {
        return arrayOf(
            openEnvItem,
            JSeparator(),
            openMapItem,
            availableMapsItem,
            JSeparator(),
            saveItem,
            JSeparator(),
            exitMenuItem
        )
    }

    private fun createMenuItem(text: String, isEnabled: Boolean = true): JMenuItem {
        return JMenuItem(text).apply {
            font = font.deriveFont(Font.PLAIN)
            this.isEnabled = isEnabled
        }
    }

    private fun createMenu(name: String, items: Array<JComponent>): JComponent = JMenu(name).apply {
        items.forEach { add(it) }
    }
}
