package io.github.spair.strongdmm.gui.common

import io.github.spair.strongdmm.gui.PrimaryFrame
import java.awt.BorderLayout
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.concurrent.thread

object Dialog {

    const val MAP_SAVE_YES: Int = 0
    const val MAP_SAVE_NO: Int = 1
    const val MAP_SAVE_CANCEL: Int = 2

    fun chooseFile(desc: String, ext: String, root: String = System.getProperty("user.home")): File? {
        val fileChooser = JFileChooser(root).apply {
            isAcceptAllFileFilterUsed = false
            addChoosableFileFilter(FileNameExtensionFilter(desc, ext))
        }

        return if (fileChooser.showOpenDialog(PrimaryFrame) == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile
        } else {
            null
        }
    }

    // Blocks main frame and does some blocking stuff while showing indeterminate progress bar
    fun runWithProgressBar(progressText: String, action: () -> Unit) {
        val progressLabel = JLabel(progressText).apply {
            border = BorderUtil.createEmptyBorder(5)
        }

        val progressBar = JProgressBar().apply {
            isIndeterminate = true
        }

        val dialog = JDialog(PrimaryFrame, "Abandon all hope, ye who enter here", true).apply {
            add(progressLabel, BorderLayout.NORTH)
            add(progressBar, BorderLayout.SOUTH)

            setSize(300, 75)
            setLocationRelativeTo(PrimaryFrame)
            defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
        }

        thread(start = true) {
            action()
            Thread.yield() // This will make us sure that dialog became visible
            dialog.isVisible = false
            dialog.dispose()
        }

        dialog.isVisible = true
    }

    fun askToSaveMap(mapName: String): Int {
        return JOptionPane.showConfirmDialog(
            PrimaryFrame,
            "Map $mapName has been modified. Save changes?",
            "Save $mapName?",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        )
    }
}
