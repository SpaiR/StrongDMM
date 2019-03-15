package io.github.spair.strongdmm.gui.objtree

import javax.swing.tree.DefaultMutableTreeNode

class ObjectTreeNode(val nodeName: String, val icon: String, val iconState: String) : DefaultMutableTreeNode(nodeName)
class SimpleTreeNode(nodeName: String) : DefaultMutableTreeNode(nodeName)
