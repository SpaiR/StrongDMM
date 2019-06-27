package io.github.spair.strongdmm.gui.map

import io.github.spair.strongdmm.common.*
import io.github.spair.strongdmm.gui.edit.variables.ViewVariablesDialog
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.select.SelectOperation
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.action.PlaceTileItemAction
import io.github.spair.strongdmm.logic.action.SwapTileItemAction
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.Tile
import io.github.spair.strongdmm.logic.map.TileItemsComparator
import io.github.spair.strongdmm.logic.map.TileOperation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

fun MapPipeline.openTilePopup() {
    if (xMouseMap == OUT_OF_BOUNDS || yMouseMap == OUT_OF_BOUNDS) {
        return
    }

    MapView.createAndShowTilePopup(Mouse.getX(), Display.getHeight() - Mouse.getY()) { popup ->
        val dmm = selectedMapData!!.dmm
        val tile = dmm.getTile(xMouseMap, yMouseMap) ?: return@createAndShowTilePopup

        SelectOperation.depickAreaIfNotInBounds(xMouseMap, yMouseMap)

        with(popup) {
            addResetActions()
            addSeparator()
            addTileActions(dmm, tile)
            addSeparator()

            if (addOptionalSelectedInstanceActions(dmm, tile)) {
                addSeparator()
            }

            addTileItemsActions(dmm, tile)
        }
    }
}

private fun JPopupMenu.addResetActions() {
    add(JMenuItem("Undo").apply {
        isEnabled = ActionController.hasUndoActions()
        addActionListener { ActionController.undoAction() }
    })

    add(JMenuItem("Redo").apply {
        isEnabled = ActionController.hasRedoActions()
        addActionListener { ActionController.redoAction() }
    })
}

private fun JPopupMenu.addTileActions(map: Dmm, currentTile: Tile) {
    fun getPickedTiles() = SelectOperation.getPickedTiles()?.takeIf { it.isNotEmpty() }

    add(JMenuItem("Cut").apply {
        addActionListener {
            val pickedTiles = getPickedTiles()

            if (pickedTiles != null) {
                TileOperation.cut(map, pickedTiles)
            } else {
                TileOperation.cut(map, currentTile)
            }

            SelectOperation.depickArea()
            Frame.update(true)
        }
    })

    add(JMenuItem("Copy").apply {
        addActionListener {
            val pickedTiles = getPickedTiles()

            if (pickedTiles != null) {
                TileOperation.copy(pickedTiles)
            } else {
                TileOperation.copy(currentTile)
            }
        }
    })

    add(JMenuItem("Paste").apply {
        isEnabled = TileOperation.hasTileInBuffer()
        addActionListener {
            TileOperation.paste(map, currentTile.x, currentTile.y) { SelectOperation.pickArea(it) }
            Frame.update(true)
        }
    })

    add(JMenuItem("Delete").apply {
        addActionListener {
            val pickedTiles = getPickedTiles()

            if (pickedTiles != null) {
                TileOperation.delete(map, pickedTiles)
            } else {
                TileOperation.delete(map, currentTile)
            }

            SelectOperation.depickArea()
            Frame.update(true)
        }
    })

    if (SelectOperation.isPickType()) {
        add(JMenuItem("Deselect").apply {
            addActionListener {
                SelectOperation.depickArea()
            }
        })
    }
}

private fun JPopupMenu.addOptionalSelectedInstanceActions(map: Dmm, currentTile: Tile): Boolean {
    val selectedInstance = InstanceListView.selectedInstance ?: return false

    val selectedType = when {
        isType(
            selectedInstance.type,
            TYPE_TURF
        ) -> TYPE_TURF
        isType(
            selectedInstance.type,
            TYPE_AREA
        ) -> TYPE_AREA
        isType(
            selectedInstance.type,
            TYPE_MOB
        ) -> TYPE_MOB
        else -> TYPE_OBJ
    }

    val selectedTypeName = selectedType.substring(1).capitalize()

    add(JMenuItem("Delete Topmost $selectedTypeName (Shift+Click)").apply {
        addActionListener {
            val topmostItem = currentTile.findTopmostTileItem(selectedType)

            if (topmostItem != null) {
                currentTile.deleteTileItem(topmostItem)
                ActionController.addUndoAction(PlaceTileItemAction(map, currentTile.x, currentTile.y, topmostItem.id))
                Frame.update(true)
            }
        }
    })

    return true
}

private fun JPopupMenu.addTileItemsActions(map: Dmm, currentTile: Tile) {
    currentTile.getTileItems().sortedWith(TileItemsComparator).forEach { tileItem ->
        val menu = JMenu("${tileItem.getVarText(VAR_NAME)} [${tileItem.type}]").apply {
            this@addTileItemsActions.add(this)
        }

        DmiProvider.getSpriteFromDmi(tileItem.icon, tileItem.iconState, tileItem.dir)?.let { spite ->
            menu.icon = spite.scaledIcon
        }

        menu.add(JMenuItem("Make Active Object (Ctrl+Shift+Click)").apply {
            addActionListener {
                ObjectTreeView.findAndSelectItemInstance(tileItem)
            }
        })

        menu.add(JMenuItem("Reset to Default").apply {
            addActionListener {
                val newTileItem = currentTile.setTileItemVars(tileItem, null)
                ActionController.addUndoAction(SwapTileItemAction(currentTile, tileItem.id, newTileItem.id))
                Frame.update(true)
                InstanceListView.updateSelectedInstanceInfo()
            }
        })

        menu.add(JMenuItem("Delete")).apply {
            addActionListener {
                currentTile.deleteTileItem(tileItem)
                ActionController.addUndoAction(PlaceTileItemAction(map, currentTile.x, currentTile.y, tileItem.id))
                Frame.update(true)
                InstanceListView.updateSelectedInstanceInfo()
            }
        }

        menu.add(JMenuItem("View Variables")).apply {
            addActionListener {
                if (ViewVariablesDialog(currentTile, tileItem).open()) {
                    Frame.update(true)
                    InstanceListView.updateSelectedInstanceInfo()
                }
            }
        }
    }
}
