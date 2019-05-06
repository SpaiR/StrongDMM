package io.github.spair.strongdmm.gui.objtree

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener

class ObjectTreeSelectionListener(private val view: ObjectTreeView) : TreeSelectionListener {

    private val instanceListView by diInstance<InstanceListView>()

    override fun valueChanged(e: TreeSelectionEvent) {
        e.path.lastPathComponent.let {
            if (it is ObjectTreeNode) {
                instanceListView.findAndSelectInstancesByType(it.type)
                view.setType(it.type)
            }
        }
    }
}
