package io.github.spair.strongdmm.gui.edit.variables

import io.github.spair.strongdmm.common.*
import io.github.spair.strongdmm.logic.dme.DmeItem
import io.github.spair.strongdmm.logic.map.TileItem
import javax.swing.table.AbstractTableModel

class ViewVariablesModel(val tileItem: TileItem) : AbstractTableModel() {

    private val displayVars: MutableList<Var> = mutableListOf()
    private val hiddenVars: Set<String> = setOf(
        VAR_TYPE,
        VAR_PARENT_TYPE,
        VAR_VARS,
        VAR_X,
        VAR_Y,
        VAR_Z,
        VAR_CONTENTS,
        VAR_FILTERS,
        VAR_LOC,
        VAR_MAPTEXT,
        VAR_MAPTEXT_WIDTH,
        VAR_MAPTEXT_HEIGHT,
        VAR_MAPTEXT_X,
        VAR_MAPTEXT_Y,
        VAR_OVERLAYS,
        VAR_UNDERLAYS,
        VAR_VERBS,
        VAR_APPEARANCE,
        VAR_VIS_CONTENTS,
        VAR_VIS_LOCS
    )

    // Vars will be added to instance on save
    val tmpVars: MutableMap<String, String> = mutableMapOf()

    var filter: String = ""
        set(value) {
            field = value
            buildVars()
            fireTableDataChanged()
        }

    var showOnlyInstanceVars: Boolean = false
        set(value) {
            field = value
            buildVars()
            fireTableDataChanged()
        }

    init {
        buildVars()
    }

    override fun getRowCount(): Int = displayVars.size
    override fun getColumnCount(): Int = 2

    override fun getColumnName(column: Int): String = if (column == 0) "Name" else "Value"

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Val? = when (columnIndex) {
        0 -> displayVars[rowIndex].name
        1 -> displayVars[rowIndex].value
        else -> null
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = (columnIndex == 1)

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

        tmpVars.forEach { (k, v) -> addVar(k, v, true) }
        tileItem.customVars?.forEach { (k, v) -> addVar(k, v, true) }

        if (!showOnlyInstanceVars) {
            collectVars(tileItem.dmeItem)
        }

        displayVars.sortBy { it.name.get() }
    }

    private fun addVar(key: String, value: String, isInstanceVar: Boolean = false) {
        if (hiddenVars.contains(key) || (filter.isNotEmpty() && !key.contains(filter))) {
            return
        }

        if (displayVars.none { it.name.get() == key }) {
            displayVars.add(Var(VarName(key, isInstanceVar), VarValue(value, isInstanceVar)))
        }
    }

    private tailrec fun collectVars(dmeItem: DmeItem) {
        dmeItem.vars.forEach { (k, v) -> addVar(k, v) }
        val parent = dmeItem.getParent()
        if (parent != null) {
            collectVars(parent)
        }
    }
}
