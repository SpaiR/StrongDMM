package io.github.spair.strongdmm.gui.map

import io.github.spair.strongdmm.gui.edit.ViewVariablesDialog
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.dme.TYPE_AREA
import io.github.spair.strongdmm.logic.dme.TYPE_OBJ
import io.github.spair.strongdmm.logic.dme.TYPE_TURF
import io.github.spair.strongdmm.logic.dme.VAR_NAME
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.history.PlaceTileItemAction
import io.github.spair.strongdmm.logic.history.TileReplaceAction
import io.github.spair.strongdmm.logic.map.OUT_OF_BOUNDS
import io.github.spair.strongdmm.logic.map.TileItem
import io.github.spair.strongdmm.logic.map.TileOperation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JSeparator

fun MapPipeline.openTilePopup() {
    if (xMouseMap == OUT_OF_BOUNDS || yMouseMap == OUT_OF_BOUNDS) {
        return
    }

    view.createAndShowTilePopup(Mouse.getX(), Display.getHeight() - Mouse.getY()) { popup ->
        val tile = selectedMap!!.getTile(xMouseMap, yMouseMap) ?: return@createAndShowTilePopup

        popup.add(JMenuItem("Undo").apply {
            isEnabled = History.hasUndoActions()
            addActionListener { History.undoAction() }
        })

        popup.add(JMenuItem("Redo").apply {
            isEnabled = History.hasRedoActions()
            addActionListener { History.redoAction() }
        })

        popup.add(JSeparator())

        popup.add(JMenuItem("Cut").apply {
            addActionListener {
                History.addUndoAction(TileReplaceAction(selectedMap!!, tile))
                TileOperation.cut(selectedMap!!, tile)
                Frame.update(true)
            }
        })

        popup.add(JMenuItem("Copy").apply {
            addActionListener {
                TileOperation.copy(tile)
            }
        })

        popup.add(JMenuItem("Paste").apply {
            isEnabled = TileOperation.hasTileInBuffer()
            addActionListener {
                History.addUndoAction(TileReplaceAction(selectedMap!!, tile))
                TileOperation.paste(selectedMap!!, tile.x, tile.y)
                Frame.update(true)
            }
        })

        popup.add(JMenuItem("Delete").apply {
            addActionListener {
                History.addUndoAction(TileReplaceAction(selectedMap!!, tile))
                TileOperation.delete(selectedMap!!, tile)
                Frame.update(true)
            }
        })

        popup.add(JSeparator())

        // Tile items
        tile.getTileItems().forEach { tileItem ->
            val menu = JMenu("${tileItem.getVarText(VAR_NAME)} [${tileItem.type}]").apply { popup.add(this) }

            DmiProvider.getSpriteFromDmi(tileItem.icon, tileItem.iconState, tileItem.dir)?.let { spite ->
                menu.icon = spite.scaledIcon
            }

            menu.add(JMenuItem("Make Active Object (Ctrl+Shift+Click)").apply {
                addActionListener {
                    ObjectTreeView.findAndSelectItemInstance(tileItem)
                }
            })

            menu.add(JMenuItem("Delete")).apply {
                addActionListener {
                    selectedMap!!.deleteTileItem(tileItem)
                    History.addUndoAction(PlaceTileItemAction(selectedMap!!, tileItem))
                    Frame.update(true)
                    InstanceListView.updateSelectedInstanceInfo()
                }
            }

            menu.add(JMenuItem("View Variables")).apply {
                addActionListener {
                    if (ViewVariablesDialog(tileItem).open()) {
                        Frame.update(true)
                        InstanceListView.updateSelectedInstanceInfo()
                    }
                }
            }
        }
    }
}
