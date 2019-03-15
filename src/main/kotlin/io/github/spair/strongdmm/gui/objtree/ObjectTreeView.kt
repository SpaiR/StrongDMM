package io.github.spair.strongdmm.gui.objtree

import io.github.spair.strongdmm.gui.common.View
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class ObjectTreeView : View {

    private val objectTree = JTree(DefaultMutableTreeNode("No open environment")).apply {
        showsRootHandles = true
    }

    override fun init(): JComponent = objectTree

    fun populateTree(vararg nodes: DefaultMutableTreeNode) {
        with(objectTree) {
            isRootVisible = true
            nodes.forEach { (objectTree.model.root as DefaultMutableTreeNode).add(it) }
            expandRow(0)
            isRootVisible = false
        }
    }
}
