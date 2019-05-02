package io.github.spair.strongdmm.gui.objtree

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.instancelist.InstanceListController
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener

class ObjectTreeSelectionListener(private val view: ObjectTreeView) : TreeSelectionListener {

    private val instanceListController by diInstance<InstanceListController>()

    override fun valueChanged(e: TreeSelectionEvent) {
        e.path.lastPathComponent.let {
            if (it is ObjectTreeNode) {
                instanceListController.findAndSelectInstancesByType(it.type)
                view.setType(it.type)
            }
        }
    }
}
