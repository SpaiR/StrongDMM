package io.github.spair.strongdmm.gui.objtree

import javax.swing.tree.DefaultMutableTreeNode

class SimpleTreeNode(nodeName: String) : DefaultMutableTreeNode(nodeName)

class ObjectTreeNode(
    val nodeName: String,
    val type: String,
    val icon: String,
    val iconState: String
) : DefaultMutableTreeNode(nodeName)
