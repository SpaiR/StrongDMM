package io.github.spair.strongdmm.gui.util

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

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
