package io.github.spair.strongdmm.gui.objtree

import io.github.spair.strongdmm.gui.TabbedObjectPanelView
import io.github.spair.strongdmm.gui.View
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.logic.EnvCleanable
import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.map.TileItem
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

object ObjectTreeView : View, EnvCleanable {

    private var searchPath = ""
    private var foundNodes: List<DefaultMutableTreeNode>? = null

    private val objectTree = JTree(SimpleTreeNode("No open environment")).apply {
        showsRootHandles = true
        cellRenderer = ObjectTreeRenderer()

        addTreeSelectionListener { e ->
            e.path.lastPathComponent.let {
                if (it is ObjectTreeNode) {
                    InstanceListView.findAndSelectInstancesByType(it.type)
                    TabbedObjectPanelView.setType(it.type)
                }
            }
        }
    }

    override fun clean() {
        foundNodes = null
        objectTree.model = DefaultTreeModel(SimpleTreeNode("Loading new environment..."))
        objectTree.isRootVisible = true
    }

    override fun initComponent() = JPanel(BorderLayout()).apply {
        add(JScrollPane(objectTree), BorderLayout.CENTER)
        add(JPanel().apply {
            preferredSize = Dimension(Int.MAX_VALUE, 60)
            addSearchRow()
        }, BorderLayout.SOUTH)
    }

    fun findAndSelectItemInstance(tileItem: TileItem) {
        findAndSelectPath(tileItem.type, true)
        InstanceListView.selectInstanceByCustomVars(tileItem.customVars)
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

        with(objectTree) {
            isRootVisible = true
            arrayOf(areaRoot, turfRoot, objRoot, mobRoot).forEach { (model.root as DefaultMutableTreeNode).add(it) }
            expandRow(0)
            isRootVisible = false
        }
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
        return ObjectTreeNode(nodeName, dmeItem.type, icon, iconState)
    }

    private fun findAndSelectPath(typePath: String, update: Boolean = false) {
        if (typePath.isEmpty()) {
            return
        }

        val nodes: List<DefaultMutableTreeNode>

        if (update || foundNodes == null) {
            val e = (objectTree.model.root as DefaultMutableTreeNode).depthFirstEnumeration()
            nodes = mutableListOf()

            while (e.hasMoreElements()) {
                val node = e.nextElement()

                if (node is SimpleTreeNode) {
                    nodes.add(node)
                    continue
                }

                (node as ObjectTreeNode).takeIf { it.type.contains(typePath) }?.let {
                    nodes.add(it)
                }
            }

            nodes.sortBy { if (it is ObjectTreeNode) it.type else it.toString() }
            foundNodes = nodes
        } else {
            nodes = foundNodes!!
        }

        if (nodes.isEmpty()) {
            return
        }

        var path = nodes[0].path

        if (!update) {
            objectTree.selectionPath?.lastPathComponent?.let { selectedNode ->
                for (i in 0..nodes.size) {
                    if ((i + 1) < nodes.size && nodes[i] === selectedNode) {
                        path = nodes[i + 1].path
                        break
                    }
                }
            }
        }

        TreePath(path).let {
            objectTree.selectionPath = it
            objectTree.scrollPathToVisible(it)
        }
    }

    private fun JPanel.addSearchRow() {
        add(JButton("-").apply {
            border = EmptyBorder(4, 10, 4, 10)

            addActionListener {
                var row = objectTree.rowCount - 1
                while (row-- > 0) {
                    objectTree.collapseRow(row)
                }
            }
        })

        add(JTextField("", 24).apply {
            document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent) = changedUpdate(e)
                override fun removeUpdate(e: DocumentEvent) = changedUpdate(e)
                override fun changedUpdate(e: DocumentEvent) {
                    searchPath = text
                    foundNodes = null
                }
            })

            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if(e.keyCode == KeyEvent.VK_ENTER) {
                        findAndSelectPath(searchPath)
                    }
                }
            })
        })

        add(JButton("Search").apply {
            addActionListener {
                findAndSelectPath(searchPath)
            }
        })
    }
}
