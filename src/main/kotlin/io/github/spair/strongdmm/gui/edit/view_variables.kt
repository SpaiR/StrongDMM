package io.github.spair.strongdmm.gui.edit

import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.history.EditVarsAction
import io.github.spair.strongdmm.logic.history.addUndoAction
import io.github.spair.strongdmm.logic.map.TileItem
import io.github.spair.strongdmm.primaryFrame
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

private val HIDDEN_VARS = setOf(
    VAR_TYPE, VAR_PARENT_TYPE, VAR_VARS, VAR_X, VAR_Y, VAR_Z, VAR_CONTENTS, VAR_FILTERS,
    VAR_LOC, VAR_MAPTEXT, VAR_MAPTEXT_WIDTH, VAR_MAPTEXT_HEIGHT, VAR_MAPTEXT_X, VAR_MAPTEXT_Y, VAR_OVERLAYS,
    VAR_UNDERLAYS, VAR_VERBS, VAR_APPEARANCE, VAR_VIS_CONTENTS, VAR_VIS_LOCS
)

class ViewVariablesDialog(private val tileItem: TileItem) {

    private var saveChanges = false
    private val dialog = JDialog(primaryFrame(), "View Variables: ${tileItem.type}", true)

    fun open(): Boolean {
        val model = ViewVariablesModel(tileItem)
        val table = JTable(model).apply {
            setDefaultRenderer(Any::class.java, ViewVariablesRenderer())
            autoCreateRowSorter = true
            tableHeader.reorderingAllowed = false
        }

        with(dialog) {
            rootPane.border = EmptyBorder(5, 5, 5, 5)

            with(contentPane) {
                add(createFilterField(model), BorderLayout.NORTH)
                add(JScrollPane(table).apply { border = EmptyBorder(2, 0, 0, 0) }, BorderLayout.CENTER)
                add(createBottomPanel(model), BorderLayout.SOUTH)
            }

            setSize(400, 450)
            setLocationRelativeTo(primaryFrame())
            isVisible = true
            dispose()
        }

        if (table.isEditing) {
            table.cellEditor.stopCellEditing()
        }

        if (saveChanges) {
            addUndoAction(EditVarsAction(tileItem))
            model.tmpVars.forEach { k, v -> tileItem.customVars[k] = v }
            tileItem.updateFields()
        }

        return saveChanges
    }

    private fun createFilterField(model: ViewVariablesModel) = JTextField().apply {
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = changedUpdate(e)
            override fun removeUpdate(e: DocumentEvent) = changedUpdate(e)
            override fun changedUpdate(e: DocumentEvent) {
                model.filter = text
            }
        })
    }

    private fun createBottomPanel(model: ViewVariablesModel) = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JCheckBox().apply { addActionListener { model.showOnlyInstanceVars = isSelected } })
            add(JLabel("Show instance vars"))
        })

        add(JPanel(BorderLayout()).apply {
            add(JButton("OK").apply { addActionListener { closeDialog(true) } }, BorderLayout.WEST)
            add(JButton("Cancel").apply { addActionListener { closeDialog(false) } }, BorderLayout.EAST)
        })
    }

    private fun closeDialog(saveChanges: Boolean) {
        this.saveChanges = saveChanges
        dialog.isVisible = false
    }
}

private class ViewVariablesRenderer : DefaultTableCellRenderer() {

    private val defaultFont = font.deriveFont(Font.PLAIN)
    private val boldFont = font.deriveFont(Font.BOLD)
    private val emptyBorder = EmptyBorder(5, 5, 5, 5)

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

        border = emptyBorder
        font = if (value is Val && value.isInstanceVar()) boldFont else defaultFont
        foreground = if (value is VarName) Color.RED else Color.BLACK

        return c
    }
}

private class ViewVariablesModel(val tileItem: TileItem) : AbstractTableModel() {

    private val displayVars = mutableListOf<Var>()

    val tmpVars = mutableMapOf<String, String>() // vars will be added to instance on save

    var filter: String = ""
        set(value) {
            field = value
            buildVars()
            fireTableDataChanged()
        }

    var showOnlyInstanceVars = false
        set(value) {
            field = value
            buildVars()
            fireTableDataChanged()
        }

    init {
        buildVars()
    }

    override fun getRowCount() = displayVars.size
    override fun getColumnCount() = 2

    override fun getValueAt(rowIndex: Int, columnIndex: Int) = when (columnIndex) {
        0 -> displayVars[rowIndex].name
        1 -> displayVars[rowIndex].value
        else -> null
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int) = columnIndex == 1

    override fun setValueAt(aValue: Any, rowIndex: Int, columnIndex: Int) {
        if (displayVars[rowIndex].value.get() == aValue) {
            return
        }

        val name = displayVars[rowIndex].name.get()
        val value = aValue.toString().trim().let { if (it.isEmpty()) "null" else it }

        tmpVars[name] = value
        buildVars()
    }

    private fun buildVars() {
        displayVars.clear()

        tmpVars.forEach { k, v -> addVar(k, v, true) }
        tileItem.customVars.forEach { k, v -> addVar(k, v, true) }

        if (!showOnlyInstanceVars) {
            collectVars(tileItem.dmeItem)
        }

        displayVars.sortBy { v -> v.name.get() }
    }

    private fun addVar(key: String, value: String, isInstanceVar: Boolean = false) {
        if (HIDDEN_VARS.contains(key) || (filter.isNotEmpty() && !key.contains(filter))) {
            return
        }

        if (displayVars.none { k -> k.name.get() == key }) {
            displayVars.add(Var(VarName(key, isInstanceVar), VarValue(value, isInstanceVar)))
        }
    }

    private fun collectVars(dmeItem: DmeItem) {
        dmeItem.vars.forEach { k, v -> addVar(k, v) }
        dmeItem.parent?.let { collectVars(it) }
    }

    override fun getColumnName(column: Int) = if (column == 0) "Name" else "Value"
}

private data class Var(val name: Val, val value: Val)

private interface Val {
    fun get(): String
    fun isInstanceVar(): Boolean
}

private abstract class StrVal(private val isInstanceVar: Boolean) : Val {
    override fun toString() = get()
    override fun isInstanceVar() = isInstanceVar
}

private class VarName(private val name: String, isInstanceVar: Boolean = false) : StrVal(isInstanceVar) {
    override fun get() = name
}

private class VarValue(private val value: String, isInstanceVar: Boolean = false) : StrVal(isInstanceVar) {
    override fun get() = value
}
