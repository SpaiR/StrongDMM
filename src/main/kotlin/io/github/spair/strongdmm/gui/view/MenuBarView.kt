package io.github.spair.strongdmm.gui.view

import java.awt.Font
import javax.swing.*

class MenuBarView : View {

    val openEnvItem = JMenuItem("Open environment").plainFont()
    val availableMapsItem = JMenuItem("Available maps").plainFont().apply { isEnabled = false }
    val exitMenuItem = JMenuItem("Exit").plainFont()

    override fun init(): JMenuBar {
        return JMenuBar().apply {
            createMenu("File", createFileItems())
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
}

private fun JMenuBar.createMenu(name: String, items: Array<JComponent>) {
    add(JMenu(name).addAll(*items))
}

private fun JMenuItem.plainFont() = apply { font = font.deriveFont(Font.PLAIN) }
