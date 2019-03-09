package io.github.spair.strongdmm.gui.util

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.kodein
import org.kodein.di.direct
import org.kodein.di.erased.instance
import java.awt.BorderLayout
import java.io.File
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.concurrent.thread

fun chooseFileDialog(desc: String, ext: String, root: String = "."): File? {
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

inline fun runWithProgressBar(progressText: String, crossinline action: () -> Unit) {
    val frame = kodein.direct.instance<PrimaryFrame>().windowFrame

    val dialog = JDialog(frame, null, true).apply {
        add(BorderLayout.NORTH, JLabel(progressText).apply { border = EmptyBorder(5, 5, 5, 5) })
        add(BorderLayout.SOUTH, JProgressBar().apply { isIndeterminate = true })

        setSize(300, 75)
        setLocationRelativeTo(frame)
        defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
    }

    thread(start = true) {
        action()
        dialog.isVisible = false
    }

    dialog.isVisible = true
}
