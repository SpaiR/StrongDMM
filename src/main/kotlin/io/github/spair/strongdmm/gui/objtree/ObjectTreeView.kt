package io.github.spair.strongdmm.gui.objtree

import io.github.spair.strongdmm.common.*
import io.github.spair.strongdmm.gui.TabbedObjectPanelView
import io.github.spair.strongdmm.gui.common.BorderUtil
import io.github.spair.strongdmm.gui.common.View
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.logic.EnvCleanable
import io.github.spair.strongdmm.logic.dme.Dme
import io.github.spair.strongdmm.logic.dme.DmeItem
import io.github.spair.strongdmm.logic.map.TileItem
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

object ObjectTreeView : View, EnvCleanable {

    private var searchPath: String = ""
    private var cachedNodes: List<ObjectTreeNode>? = null

    private val objectTree = JTree(SimpleTreeNode("No open environment"))

    init {
        with(objectTree) {
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
    }

    override fun clean() {
        cachedNodes = null
        objectTree.model = DefaultTreeModel(SimpleTreeNode("Loading new environment..."))
        objectTree.isRootVisible = true
    }

    override fun initComponent(): JPanel = JPanel(BorderLayout()).apply {
        add(JScrollPane(objectTree), BorderLayout.CENTER)
        add(JPanel().apply {
            border = BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY)
            preferredSize = Dimension(Int.MAX_VALUE, 40)
            addSearchRow()
        }, BorderLayout.SOUTH)
    }

    fun findAndSelectItemInstance(tileItem: TileItem) {
        findAndSelectPath(tileItem.type, update = true, strict = true)
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

    private fun findAndSelectPath(typePath: String, update: Boolean = false, strict: Boolean = false) {
        if (typePath.isEmpty()) {
            return
        }

        val nodes: List<ObjectTreeNode>

        if (update || cachedNodes == null) {
            val e = (objectTree.model.root as DefaultMutableTreeNode).depthFirstEnumeration()
            val foundNodes = mutableListOf<ObjectTreeNode>()

            while (e.hasMoreElements()) {
                val nextElement = e.nextElement()

                if (nextElement is SimpleTreeNode) {
                    continue
                }

                val node = nextElement as ObjectTreeNode

                if (node.type.contains(typePath)) {
                    foundNodes.add(node)
                    if (strict && node.type == typePath) {
                        break
                    }
                }
            }

            nodes = if (strict) {
                foundNodes.filter { it.type == typePath }
            } else {
                foundNodes.sortedWith(ObjectTreeNodeComparator)
            }

            cachedNodes = nodes
        } else {
            nodes = cachedNodes!!
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
            border = BorderUtil.createEmptyBorder(4, 10, 4, 10)

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
                    cachedNodes = null
                }
            })

            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
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
