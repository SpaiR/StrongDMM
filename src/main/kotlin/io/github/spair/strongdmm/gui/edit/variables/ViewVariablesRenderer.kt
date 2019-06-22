package io.github.spair.strongdmm.gui.edit.variables

import io.github.spair.strongdmm.gui.common.BorderUtil
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JTable
import javax.swing.border.Border
import javax.swing.table.DefaultTableCellRenderer

class ViewVariablesRenderer : DefaultTableCellRenderer() {

    private val defaultFont: Font = font.deriveFont(Font.PLAIN)
    private val boldFont: Font = font.deriveFont(Font.BOLD)
    private val emptyBorder: Border = BorderUtil.createEmptyBorder(5)

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
