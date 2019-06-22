package io.github.spair.strongdmm.gui.edit

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.map.LayersManager
import org.scijava.swing.checkboxtree.CheckBoxNodeData
import org.scijava.swing.checkboxtree.CheckBoxNodeEditor
import org.scijava.swing.checkboxtree.CheckBoxNodeRenderer
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class LayersFilter {

    private val dialog = JDialog(PrimaryFrame, "Layers Filter", true)
    private val typesModel = DefaultTreeModel(DefaultMutableTreeNode("invisible root"))
    private val typesTree = JTree(typesModel)

    private var searchPath = ""
    private var foundNodes: List<DefaultMutableTreeNode>? = null

    init {
        populateTree()

        with(typesTree) {
            expandRow(0)
            showsRootHandles = true
            cellRenderer = CheckBoxNodeRenderer()
            cellEditor = CheckBoxNodeEditor(typesTree)
            isEditable = true
            isRootVisible = false
        }

        // to listen checkbox events
        typesModel.addTreeModelListener(object : TreeModelListener {
            override fun treeNodesInserted(e: TreeModelEvent) {}
            override fun treeStructureChanged(e: TreeModelEvent) {}
            override fun treeNodesRemoved(e: TreeModelEvent) {}
            override fun treeNodesChanged(e: TreeModelEvent) = handleNodeChecked()
        })
    }

    fun open() {
        with(dialog) {
            with(contentPane) {
                add(addSearchRow(), BorderLayout.NORTH)
                add(JScrollPane(typesTree))
            }

            setSize(500, 600)
            setLocationRelativeTo(PrimaryFrame)
            isVisible = true
            dispose()
        }
    }

    private fun populateTree() {
        val dme = Environment.dme

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

        arrayOf(areaRoot, turfRoot, objRoot, mobRoot).forEach {
            (typesModel.root as DefaultMutableTreeNode).add(it)
        }
    }

    private fun getListOfSubtypes(dmeItem: DmeItem, dme: Dme): List<DefaultMutableTreeNode> {
        val childList = mutableListOf<DefaultMutableTreeNode>()

        for (subtype in dmeItem.children) {
            val currentItem = dme.getItem(subtype)!!
            val currentRoot = createTreeNode(currentItem)
            childList.add(currentRoot)
            getListOfSubtypes(currentItem, dme).forEach { currentRoot.add(it) }
        }

        return childList.sortedBy { it.toString() }
    }

    private fun createTreeNode(dmeItem: DmeItem): DefaultMutableTreeNode {
        return DefaultMutableTreeNode(CheckBoxNodeData(dmeItem.type, !LayersManager.isHiddenType(dmeItem.type)))
    }

    private fun handleNodeChecked() {
        val node = typesTree.lastSelectedPathComponent as DefaultMutableTreeNode
        val checkBoxNode = (node.userObject as CheckBoxNodeData)
        val mainType = checkBoxNode.text
        val isChecked = checkBoxNode.isChecked

        var isMainTypeInVisible = false

        if (isChecked) {
            if (LayersManager.isInHiddenTypes(mainType)) {
                LayersManager.removeHiddenType(mainType)
            }
            if (LayersManager.isHiddenType(mainType)) {
                LayersManager.addVisibleType(mainType)
                isMainTypeInVisible = true
            }
        } else {
            if (LayersManager.isInVisibleTypes(mainType)) {
                LayersManager.removeVisibleType(mainType)
            } else {
                LayersManager.addHiddenType(mainType)
            }
        }

        if (mainType in arrayOf(TYPE_AREA, TYPE_TURF, TYPE_OBJ, TYPE_MOB)) {
            MenuBarView.switchBasicLayers(mainType, isChecked)
        }

        val children = node.breadthFirstEnumeration()

        while (children.hasMoreElements()) {
            val childNode = children.nextElement() as DefaultMutableTreeNode

            if (childNode === node) {
                continue
            }

            val childCheckBox = childNode.userObject as CheckBoxNodeData
            val childType = childCheckBox.text

            if (isChecked) {
                if (isMainTypeInVisible) {
                    LayersManager.addVisibleType(childType)
                } else {
                    LayersManager.removeVisibleType(childType)
                }

                LayersManager.removeHiddenType(childType)
            } else {
                LayersManager.removeVisibleType(childType)
            }

            childCheckBox.isChecked = !LayersManager.isHiddenType(childType)
        }

        typesTree.repaint()
        Frame.update(true)
    }

    private fun addSearchRow() = JPanel().apply {
        add(JButton("-").apply {
            border = EmptyBorder(4, 10, 4, 10)
            addActionListener {
                foundNodes = null
                var row = typesTree.rowCount - 1
                while (row-- > 0) {
                    typesTree.collapseRow(row)
                }
            }
        })

        add(JTextField("", 32).apply {
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
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        findAndSelectPath()
                    }
                }
            })
        })

        add(JButton("Search").apply {
            addActionListener { findAndSelectPath() }
        })

        add(JButton("Reset").apply {
            toolTipText = "This will reset all filters you've set up"
            addActionListener {
                LayersManager.reset()

                val children = (typesModel.root as DefaultMutableTreeNode).depthFirstEnumeration()

                while (children.hasMoreElements()) {
                    val child = children.nextElement() as DefaultMutableTreeNode

                    if (child === typesModel.root) {
                        continue
                    }

                    val childCheckBox = child.userObject as CheckBoxNodeData
                    childCheckBox.isChecked = true
                }

                typesModel.reload()

                MenuBarView.switchBasicLayers(TYPE_AREA, true)
                MenuBarView.switchBasicLayers(TYPE_TURF, true)
                MenuBarView.switchBasicLayers(TYPE_OBJ, true)
                MenuBarView.switchBasicLayers(TYPE_MOB, true)

                Frame.update(true)
            }
        })
    }

    private fun findAndSelectPath() {
        if (searchPath.isEmpty()) {
            return
        }

        val nodes: List<DefaultMutableTreeNode>

        if (foundNodes == null) {
            val e = (typesTree.model.root as DefaultMutableTreeNode).breadthFirstEnumeration()
            nodes = mutableListOf()

            while (e.hasMoreElements()) {
                val node = e.nextElement()

                (node as DefaultMutableTreeNode).takeIf { it.toString().contains(searchPath) }?.let {
                    nodes.add(it)
                }
            }

            foundNodes = nodes
        } else {
            nodes = foundNodes!!
        }

        if (nodes.isEmpty()) {
            return
        }

        var path = nodes[0].path

        typesTree.selectionPath?.lastPathComponent?.let { selectedNode ->
            for (i in 0..nodes.size) {
                if ((i + 1) < nodes.size && nodes[i] === selectedNode) {
                    path = nodes[i + 1].path
                    break
                }
            }
        }

        TreePath(path).let {
            typesTree.selectionPath = it
            typesTree.scrollPathToVisible(it)
        }
    }
}
