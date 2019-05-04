package io.github.spair.strongdmm.gui.objtree

import io.github.spair.strongdmm.gui.View
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ObjectTreeView : View {

    private var searchPath = ""
    private var foundNodes: List<DefaultMutableTreeNode>? = null
    private val typeField = JTextField("no type selected", 37).apply {
        isEditable = false
        border = EmptyBorder(0, 0, 0, 0)
    }

    private val objectTree = JTree(SimpleTreeNode("No open environment")).apply {
        showsRootHandles = true
        cellRenderer = ObjectTreeRenderer()
        addTreeSelectionListener(ObjectTreeSelectionListener(this@ObjectTreeView))
    }

    override fun init() = JPanel(BorderLayout()).apply {
        add(JScrollPane(objectTree), BorderLayout.CENTER)
        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            preferredSize = Dimension(Int.MAX_VALUE, 60)
            addSearchRow()
            add(JLabel("<html><b>type:</b></html>"))
            add(typeField)
        }, BorderLayout.SOUTH)
    }

    fun populateTree(vararg nodes: ObjectTreeNode) {
        with(objectTree) {
            isRootVisible = true
            nodes.forEach { (model.root as DefaultMutableTreeNode).add(it) }
            expandRow(0)
            isRootVisible = false
        }
    }

    fun setType(type: String) {
        typeField.text = type
    }

    fun findAndSelectPath(typePath: String, update: Boolean = false) {
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

        objectTree.selectionPath?.lastPathComponent?.let { selectedNode ->
            for (i in 0..nodes.size) {
                if ((i + 1) < nodes.size && nodes[i] === selectedNode) {
                    path = nodes[i + 1].path
                    break
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

        add(JTextField("", 28).apply {
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
