package io.github.spair.strongdmm.gui.edit.variables

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.common.BorderUtil
import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.action.ReplaceTileItemAction
import io.github.spair.strongdmm.logic.map.Tile
import io.github.spair.strongdmm.logic.map.TileItem
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ViewVariablesDialog(private val tile: Tile, private val tileItem: TileItem, private val absIdx: Int) {

    private var saveChanges: Boolean = false
    private val dialog: JDialog = JDialog(PrimaryFrame, "Edit Variables: ${tileItem.type}", true)

    private val vvModel: ViewVariablesModel = ViewVariablesModel(tileItem)
    private val varsTable: JTable = JTable(vvModel)

    init {
        with(varsTable) {
            setDefaultRenderer(Any::class.java, ViewVariablesRenderer())
            autoCreateRowSorter = true
            tableHeader.reorderingAllowed = false
        }
    }

    fun open(): Boolean {
        with(dialog) {
            rootPane.border = BorderUtil.createEmptyBorder(5)

            with(contentPane) {
                add(createFilterField(), BorderLayout.NORTH)
                add(createScrollPane(), BorderLayout.CENTER)
                add(createBottomPanel(), BorderLayout.SOUTH)
            }

            setSize(400, 450)
            setLocationRelativeTo(PrimaryFrame)
            isVisible = true
            dispose()
        }

        if (varsTable.isEditing) {
            varsTable.cellEditor.stopCellEditing()
        }

        if (saveChanges && vvModel.tmpVars.isNotEmpty()) {
            tile.addTileItemVars(absIdx, vvModel.tmpVars)
            ActionController.addUndoAction(ReplaceTileItemAction(tile, absIdx, tileItem.id))
        }

        return saveChanges
    }

    private fun createFilterField() = JTextField().apply {
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = changedUpdate(e)
            override fun removeUpdate(e: DocumentEvent) = changedUpdate(e)
            override fun changedUpdate(e: DocumentEvent) {
                vvModel.filter = text
            }
        })
    }

    private fun createScrollPane(): JComponent {
        return JScrollPane(varsTable).apply {
            border = BorderUtil.createEmptyBorder(top = 5)
        }
    }

    private fun createBottomPanel() = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JCheckBox().apply {
                addActionListener {
                    vvModel.showOnlyInstanceVars = isSelected
                }
            })

            add(JLabel("Show instance vars"))
        })

        add(JPanel(BorderLayout()).apply {
            add(JButton("OK").apply {
                addActionListener {
                    closeDialog(true)
                }
            }, BorderLayout.WEST)

            add(JButton("Cancel").apply {
                addActionListener {
                    closeDialog(false)
                }
            }, BorderLayout.EAST)
        })
    }

    private fun closeDialog(saveChanges: Boolean) {
        this.saveChanges = saveChanges
        dialog.isVisible = false
    }
}
