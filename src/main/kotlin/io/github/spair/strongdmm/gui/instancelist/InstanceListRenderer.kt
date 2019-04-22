package io.github.spair.strongdmm.gui.instancelist

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.ImageIcon
import javax.swing.JList

class InstanceListRenderer : DefaultListCellRenderer() {

    private val placeholderIcon = ImageIcon(DmiProvider.PLACEHOLDER_IMAGE)
    private val dmiProvider by diInstance<DmiProvider>()

    override fun getListCellRendererComponent(
        list: JList<*>,
        value: Any,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        val instance = value as ListItemInstance

        name = instance.name
        icon = dmiProvider.getDmi(instance.icon)?.getIconState(instance.iconState)?.getIconSprite(instance.dir) ?: placeholderIcon

        return this
    }
}