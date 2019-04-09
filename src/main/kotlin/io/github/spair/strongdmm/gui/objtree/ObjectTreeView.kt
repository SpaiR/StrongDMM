package io.github.spair.strongdmm.gui.objtree

import io.github.spair.strongdmm.gui.View
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class ObjectTreeView : View {

    val objectTree = JTree(SimpleTreeNode("No open environment")).apply {
        showsRootHandles = true
    }

    override fun init() = JScrollPane(objectTree)

    fun populateTree(vararg nodes: ObjectTreeNode) {
        with(objectTree) {
            isRootVisible = true
            nodes.forEach { (model.root as DefaultMutableTreeNode).add(it) }
            expandRow(0)
            isRootVisible = false
        }
    }
}
