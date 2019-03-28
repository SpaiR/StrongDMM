package io.github.spair.strongdmm.gui.edit

import io.github.spair.strongdmm.logic.dme.DmeItem
import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.map.TileItem
import io.github.spair.strongdmm.primaryFrame
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JDialog
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

private val HIDDEN_VARS = setOf(
    VAR_TYPE, VAR_PARENT_TYPE, VAR_VARS, VAR_X, VAR_Y, VAR_Z, VAR_CONTENTS, VAR_FILTERS,
    VAR_LOC, VAR_MAPTEXT, VAR_MAPTEXT_WIDTH, VAR_MAPTEXT_HEIGHT, VAR_MAPTEXT_X, VAR_MAPTEXT_Y, VAR_OVERLAYS,
    VAR_UNDERLAYS, VAR_VERBS, VAR_APPEARANCE, VAR_VIS_CONTENTS, VAR_VIS_LOCS
)

class ViewVariablesListener(private val tileItem: TileItem) : ActionListener {
    override fun actionPerformed(e: ActionEvent) {
        val dialog = JDialog(primaryFrame(), "View Variables", true)

        val table = JTable(ViewVariablesModel(tileItem)).apply {
            setDefaultRenderer(Any::class.java, ViewVariablesRenderer())
            autoCreateRowSorter = true
            tableHeader.reorderingAllowed = false
        }

        with(dialog) {
            contentPane.add(JScrollPane(table))
            setSize(400, 450)
            setLocationRelativeTo(primaryFrame())
            isVisible = true
            dispose()
        }
    }
}

private class ViewVariablesRenderer : DefaultTableCellRenderer() {

    private val defaultFont = font.deriveFont(Font.PLAIN)
    private val boldFont = font.deriveFont(Font.BOLD)

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        foreground = Color.BLACK

        val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

        if (value is Val && value.isInstanceVar()) {
            c.font = boldFont
        } else {
            c.font = defaultFont
        }

        if (value is VarName) {
            c.foreground = Color.RED
        }

        return c
    }
}

private class ViewVariablesModel(val tileItem: TileItem) : AbstractTableModel() {

    private val vars = mutableListOf<Var>()

    init {
        tileItem.customVars.forEach { k, v -> addVar(k, v, true) }
        collectVars(tileItem.dmeItem)
        vars.sortBy { v -> v.name.get() }
    }

    override fun getRowCount() = vars.size
    override fun getColumnCount() = 2

    override fun getValueAt(rowIndex: Int, columnIndex: Int) = when (columnIndex) {
        0 -> vars[rowIndex].name
        1 -> vars[rowIndex].value
        else -> null
    }

    private fun addVar(key: String, value: String?, isInstanceVar: Boolean = false) {
        if (!HIDDEN_VARS.contains(key)) {
            vars.add(Var(VarName(key, isInstanceVar), VarValue(value ?: "null", isInstanceVar)))
        }
    }

    private fun collectVars(dmeItem: DmeItem) {
        dmeItem.vars.forEach { k, v ->
            if (vars.none { key -> key.name.get() == k }) {
                addVar(k, v)
            }
        }

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
    override fun toString() = " ${get()}"
    override fun isInstanceVar() = isInstanceVar
}

private class VarName(private val name: String, isInstanceVar: Boolean = false) : StrVal(isInstanceVar) {
    override fun get() = name
}

private class VarValue(private val value: String, isInstanceVar: Boolean = false) : StrVal(isInstanceVar) {
    override fun get() = value
}
