package io.github.spair.strongdmm.gui.common

import io.github.spair.strongdmm.gui.PrimaryFrame
import java.awt.BorderLayout
import java.io.File
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.concurrent.thread

object Dialog {

    fun chooseFile(desc: String, ext: String, root: String = "."): File? {
        val fileChooser = JFileChooser(root).apply {
            isAcceptAllFileFilterUsed = false
            addChoosableFileFilter(FileNameExtensionFilter(desc, ext))
        }

        return if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
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

        val dialog = JDialog(PrimaryFrame, null, true).apply {
            add(progressLabel, BorderLayout.NORTH)
            add(progressBar, BorderLayout.SOUTH)

            setSize(300, 75)
            setLocationRelativeTo(PrimaryFrame)
            defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
        }

        thread(start = true) {
            action()
            dialog.isVisible = false
            dialog.dispose()
        }

        dialog.isVisible = true
    }
}
