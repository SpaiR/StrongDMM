package io.github.spair.strongdmm.gui.common

import io.github.spair.strongdmm.gui.PrimaryFrame
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.File
import javax.swing.*
import javax.swing.event.HyperlinkEvent
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

    fun createFile(desc: String, ext: String, root: String = System.getProperty("user.home")): File? {
        val fileChooser = JFileChooser(root).apply {
            isAcceptAllFileFilterUsed = false
            addChoosableFileFilter(FileNameExtensionFilter(desc, ext))
        }

        return if (fileChooser.showSaveDialog(PrimaryFrame) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile

            if (!file.exists()) {
                file.createNewFile()
            }

            file
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

    fun showHtmlContent(title: String, resPath: String, width: Int = 500, height: Int = 500) {
        val textPane = JEditorPane().apply {
            border = BorderUtil.createEmptyBorder()
            contentType = "text/html"
            text = Dialog::class.java.classLoader.getResource(resPath)!!.readText()
            isEditable = false

            addHyperlinkListener {
                if (HyperlinkEvent.EventType.ACTIVATED == it.eventType) {
                    Desktop.getDesktop().browse(it.url.toURI())
                }
            }
        }

        JDialog(PrimaryFrame, title, true).apply {
            size = Dimension(width, height)
            setLocationRelativeTo(PrimaryFrame)
            add(textPane)
            isVisible = true
            dispose()
        }
    }

    fun askMapSize(initX: Int, initY: Int): Pair<Int, Int>? {
        val xEditField = JTextField("$initX", 5)
        val yEditField = JTextField("$initY", 5)

        JDialog(PrimaryFrame, "Set Map Size", true).apply {
            val dialog = this

            size = Dimension(200, 115)
            setLocationRelativeTo(PrimaryFrame)

            add(JPanel(FlowLayout(FlowLayout.CENTER)).apply {
                add(JPanel().apply {
                    add(JLabel("X:"))
                    add(xEditField)
                })
                add(JPanel().apply {
                    add(JLabel("Y:"))
                    add(yEditField)
                })
            })
            add(JPanel().apply {
                add(JButton("OK").apply {
                    preferredSize = Dimension(80, 20)
                    addActionListener {
                        dialog.isVisible = false
                    }
                })
            }, BorderLayout.SOUTH)

            isVisible = true
            dispose()
        }

        val x = xEditField.text.toIntOrNull()
        val y = yEditField.text.toIntOrNull()

        return if ((x != null && x > 0) && (y != null && y > 0)) Pair(x, y) else null
    }
}
