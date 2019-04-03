package io.github.spair.strongdmm.gui.objtree

import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener

object ObjectTreeSelectionListener : TreeSelectionListener {

    override fun valueChanged(e: TreeSelectionEvent) {
        e.path.lastPathComponent.let {
            if (it is ObjectTreeNode) {
                println(it.type)
            }
        }
    }
}
