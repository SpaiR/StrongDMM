package io.github.spair.strongdmm.gui.view

import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class ObjectTreeView : View {

    val objectTree = JTree(DefaultMutableTreeNode("root"))

    override fun init(): JComponent {
        return objectTree.apply {
            (objectTree.model.root as DefaultMutableTreeNode).let { root ->
                root.add(DefaultMutableTreeNode("123").apply {
                    add(DefaultMutableTreeNode("child1"))
                })
                root.add(DefaultMutableTreeNode("456").apply {
                    add(DefaultMutableTreeNode("child2"))
                })
                root.add(DefaultMutableTreeNode("789").apply {
                    add(DefaultMutableTreeNode("child3"))
                })
            }
            expandRow(0)
            isRootVisible = false
            showsRootHandles = true
        }
    }
}
