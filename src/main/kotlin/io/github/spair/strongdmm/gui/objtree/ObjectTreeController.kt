package io.github.spair.strongdmm.gui.objtree

import io.github.spair.strongdmm.DI
import io.github.spair.strongdmm.gui.ViewController
import io.github.spair.strongdmm.logic.dme.*
import org.kodein.di.direct
import org.kodein.di.erased.instance

class ObjectTreeController : ViewController<ObjectTreeView>(DI.direct.instance()) {

    override fun init() {
        view.objectTree.cellRenderer = ObjectTreeRenderer(DI.direct.instance())
    }

    fun populateTree(dme: Dme) {
        val area = dme.getItem(TYPE_AREA)!!
        val turf = dme.getItem(TYPE_TURF)!!
        val obj = dme.getItem(TYPE_OBJ)!!
        val mob = dme.getItem(TYPE_MOB)!!

        val areaRoot = createTreeNode(area)
        val turfRoot = createTreeNode(turf)
        val objRoot = createTreeNode(obj)
        val mobRoot = createTreeNode(mob)

        getListOfSubtypes(area, dme).forEach { areaRoot.add(it) }
        getListOfSubtypes(turf, dme).forEach { turfRoot.add(it) }
        getListOfSubtypes(obj, dme).forEach { objRoot.add(it) }
        getListOfSubtypes(mob, dme).forEach { mobRoot.add(it) }

        view.populateTree(areaRoot, turfRoot, objRoot, mobRoot)
    }

    private fun getListOfSubtypes(dmeItem: DmeItem, dme: Dme): List<ObjectTreeNode> {
        val childList = mutableListOf<ObjectTreeNode>()

        for (subtype in dmeItem.children) {
            val currentItem = dme.getItem(subtype)!!
            val currentRoot = createTreeNode(currentItem)
            childList.add(currentRoot)
            getListOfSubtypes(currentItem, dme).forEach { currentRoot.add(it) }
        }

        return childList.sortedBy { it.nodeName }
    }

    private fun createTreeNode(dmeItem: DmeItem): ObjectTreeNode {
        val nodeName = dmeItem.type.substringAfterLast('/')
        val icon = dmeItem.getVarText(VAR_ICON) ?: ""
        val iconState = dmeItem.getVarText(VAR_ICON_STATE) ?: ""
        return ObjectTreeNode(nodeName, icon, iconState)
    }
}
