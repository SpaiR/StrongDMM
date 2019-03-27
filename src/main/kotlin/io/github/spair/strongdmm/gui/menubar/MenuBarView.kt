package io.github.spair.strongdmm.gui.menubar

import io.github.spair.strongdmm.gui.View
import java.awt.Font
import javax.swing.*

class MenuBarView : View {

    val openEnvItem = createMenuItem("Open environment")
    val availableMapsItem = createMenuItem("Available maps", false)
    val exitMenuItem = createMenuItem("Exit")

    override fun init(): JMenuBar {
        return JMenuBar().apply {
            add(createMenu("File", createFileItems()))
        }
    }

    private fun createFileItems(): Array<JComponent> {
        return arrayOf(
            openEnvItem,
            availableMapsItem,
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
