package io.github.spair.strongdmm.gui.instancelist

import io.github.spair.strongdmm.gui.View
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.EmptyBorder

class InstanceListView : View {

    private val instanceList = JList<ListItemInstance>(DefaultListModel<ListItemInstance>()).apply {
        cellRenderer = InstanceListRenderer()

        addListSelectionListener {
            selectedValue?.let { showInstanceVars(it.customVars) }
        }
    }

    private val customVariablesLabel = JLabel()

    override fun init(): JComponent {
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

    fun addItemInstances(instances: Collection<ListItemInstance>) {
        with(instanceList.model as DefaultListModel) {
            clear()
            instances.forEach { addElement(it) }
        }
        instanceList.selectedIndex = 0
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
