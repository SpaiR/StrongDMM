package io.github.spair.strongdmm.gui.objtree

import io.github.spair.byond.ByondTypes
import io.github.spair.byond.dme.Dme
import io.github.spair.byond.dme.DmeItem
import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.common.ViewController
import org.kodein.di.direct
import org.kodein.di.erased.instance
import javax.swing.tree.DefaultMutableTreeNode

class ObjectTreeController : ViewController<ObjectTreeView>(DI.direct.instance()) {

    override fun init() {
    }

    fun populateTree(dme: Dme) {
        val area = dme.getItem(ByondTypes.AREA)
        val turf = dme.getItem(ByondTypes.TURF)
        val obj = dme.getItem(ByondTypes.OBJ)
        val mob = dme.getItem(ByondTypes.MOB)

        val areaRoot = createTreeNode(area)
        val turfRoot = createTreeNode(turf)
        val objRoot = createTreeNode(obj)
        val mobRoot = createTreeNode(mob)

        addSubtypesToRoot(areaRoot, area, dme)
        addSubtypesToRoot(turfRoot, turf, dme)
        addSubtypesToRoot(objRoot, obj, dme)
        addSubtypesToRoot(mobRoot, mob, dme)

        view.populateTree(areaRoot, turfRoot, objRoot, mobRoot)
    }

    private fun addSubtypesToRoot(root: DefaultMutableTreeNode, dmeItem: DmeItem, dme: Dme) {
        for (subtype in dmeItem.directSubtypes) {
            val currentItem = dme.getItem(subtype)
            val currentRoot = createTreeNode(currentItem)
            root.add(currentRoot)
            addSubtypesToRoot(currentRoot, currentItem, dme)
        }
    }

    private fun createTreeNode(dmeItem: DmeItem): DefaultMutableTreeNode {
        val nodeName = dmeItem.type.substringAfterLast('/')
        return DefaultMutableTreeNode(nodeName)
    }
}
