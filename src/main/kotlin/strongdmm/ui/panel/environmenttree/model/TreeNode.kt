package strongdmm.ui.panel.environmenttree.model

import strongdmm.byond.dme.DmeItem
import strongdmm.byond.dmi.IconSprite

class TreeNode(
    val dmeItem: DmeItem,
    val sprite: IconSprite
) {
    val name: String = dmeItem.type.substringAfterLast('/')
}
