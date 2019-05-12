package io.github.spair.strongdmm.gui.instancelist

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.TabbedObjectPanelView
import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasView
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.VAR_ICON
import io.github.spair.strongdmm.logic.dme.VAR_ICON_STATE
import io.github.spair.strongdmm.logic.dme.VAR_NAME
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.EmptyBorder

class InstanceListView : View {

    private val mapCanvasView by diInstance<MapCanvasView>()
    private val tabbedObjectPanelView by diInstance<TabbedObjectPanelView>()

    private var selectedInstance: ItemInstance? = null

    private val customVariablesLabel = JLabel()
    private val instanceList = JList<ItemInstance>(DefaultListModel<ItemInstance>()).apply {
        cellRenderer = InstanceListRenderer()

        addListSelectionListener {
            selectedValue?.let {
                showInstanceVars(it.customVars)
                selectedInstance = it
            }
        }
    }

    override fun initComponent(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(JScrollPane(instanceList), BorderLayout.CENTER)
            add(JPanel().apply {
                preferredSize = Dimension(Int.MAX_VALUE, 200)
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = EmptyBorder(5, 5, 5, 5)

                add(JLabel("<html><h4>Instance variables:</h4></html>"))
                add(JScrollPane(JPanel(FlowLayout(FlowLayout.LEFT)).apply { add(customVariablesLabel) }))

                setEmptyInstanceVars()
            }, BorderLayout.SOUTH)
        }
    }

    fun selectInstanceByCustomVars(customVars: Map<String, String>) {
        val model = instanceList.model as DefaultListModel<ItemInstance>
        for (i in 0 until model.size()) {
            if (model[i].customVars == customVars) {
                instanceList.selectedIndex = i
                break
            }
        }
    }

    fun findAndSelectInstancesByType(type: String) {
        val items = LinkedHashSet<ItemInstance>()
        val dmeItem = Environment.dme.getItem(type)!!
        val initialInstance = ItemInstance(
            dmeItem.getVar(VAR_NAME) ?: "",
            dmeItem.getVarText(VAR_ICON) ?: "",
            dmeItem.getVarText(VAR_ICON_STATE) ?: "",
            dmeItem.type
        )

        selectedInstance = initialInstance
        items.add(initialInstance)

        mapCanvasView.getSelectedMap()?.let { dmm ->
            dmm.getAllTileItemsByType(type).forEach {
                items.add(ItemInstance(
                    it.getVar(VAR_NAME) ?: "", it.icon, it.iconState, it.type, it.dir, it.customVars)
                )
            }
        }

        with(instanceList.model as DefaultListModel) {
            clear()
            items.forEach { addElement(it) }
        }

        instanceList.selectedIndex = 0
        tabbedObjectPanelView.setInstanceCount(items.size)
    }

    fun updateSelectedInstanceInfo() {
        if (selectedInstance != null) {
            findAndSelectInstancesByType(selectedInstance!!.type)
        }
    }

    private fun showInstanceVars(variables: Map<String, String>) {
        if (variables.isEmpty()) {
            setEmptyInstanceVars()
            return
        }

        customVariablesLabel.text = buildString {
            append("<html>")
            variables.forEach { (k, v) -> append("- <b>$k</b>: $v<br>") }
            append("</html>")
        }
    }

    private fun setEmptyInstanceVars() {
        customVariablesLabel.text = "empty (instance with initial vars)"
    }
}
