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
    VAR_TYPE, VAR_PARENT_TYPE, VAR_VARS, VAR_X, VAR_Y, VAR_Z, VAR_VIS_CONTENTS, VAR_CONTENTS, VAR_FILTERS,
    VAR_LOC, VAR_MAPTEXT, VAR_MAPTEXT_WIDTH, VAR_MAPTEXT_HEIGHT, VAR_MAPTEXT_X, VAR_MAPTEXT_Y, VAR_OVERLAYS,
    VAR_UNDERLAYS, VAR_VERBS, VAR_APPEARANCE
)

class ViewVariablesListener(private val tileItem: TileItem) : ActionListener {
    override fun actionPerformed(e: ActionEvent) {
        val dialog = JDialog(primaryFrame(), "View Variables", true)

        val table = JTable(ViewVariablesModel(tileItem)).apply {
            setDefaultRenderer(Any::class.java, ViewVariablesRenderer())
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
    private val defaultBack = background

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        background = defaultBack
        foreground = Color.BLACK

        val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

        if (value is TypeName || value is TypeValue) {
            c.font = boldFont
            c.background = Color.GRAY
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

    private val keys = mutableListOf<Val>()
    private val vals = mutableListOf<Val>()

    init {
        if (tileItem.customVars.isNotEmpty()) {
            addType("Instance variables")
            tileItem.customVars.forEach(this::addVar)
        }

        collectVars(tileItem.dmeItem)
    }

    override fun getRowCount() = keys.size
    override fun getColumnCount() = 2

    override fun getValueAt(rowIndex: Int, columnIndex: Int) = when (columnIndex) {
        0 -> keys[rowIndex]
        1 -> vals[rowIndex]
        else -> null
    }

    private fun addType(type: String) {
        keys.add(TypeName(type))
        vals.add(TypeValue())
    }

    private fun addVar(key: String, value: String?) {
        if (!HIDDEN_VARS.contains(key)) {
            keys.add(VarName(key))
            vals.add(VarValue(value ?: "null"))
        }
    }

    private fun collectVars(dmeItem: DmeItem) {
        if (dmeItem.vars.isNotEmpty()) {
            addType(dmeItem.type)
            dmeItem.vars.forEach(this::addVar)
        }

        dmeItem.parent?.let { collectVars(it) }
    }

    override fun getColumnName(column: Int) = if (column == 0) "Name" else "Value"
}

private interface Val {
    fun get(): String
}

private abstract class StrVal : Val {
    override fun toString() = " ${get()}"
}

private class TypeName(private val name: String) : StrVal() {
    override fun get() = name
}

private class TypeValue : StrVal() {
    override fun get() = ""
}

private class VarName(private val name: String) : StrVal() {
    override fun get() = name
}

private class VarValue(private val value: String) : StrVal() {
    override fun get() = value
}
