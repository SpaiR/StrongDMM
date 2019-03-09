package io.github.spair.strongdmm.gui.view

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
            nodes.forEach { objectTree.add(it) }
            expandRow(0)
            isRootVisible = false
        }
    }
}

private fun JTree.add(node: DefaultMutableTreeNode) {
    (model.root as DefaultMutableTreeNode).add(node)
}
