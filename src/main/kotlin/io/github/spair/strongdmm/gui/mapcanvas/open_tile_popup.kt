package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.gui.edit.ViewVariablesDialog
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.dme.TYPE_AREA
import io.github.spair.strongdmm.logic.dme.TYPE_OBJ
import io.github.spair.strongdmm.logic.dme.TYPE_TURF
import io.github.spair.strongdmm.logic.dme.VAR_NAME
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.history.DeleteTileItemAction
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.history.TileReplaceAction
import io.github.spair.strongdmm.logic.map.OUT_OF_BOUNDS
import io.github.spair.strongdmm.logic.map.TileItem
import io.github.spair.strongdmm.logic.map.TileOperation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JSeparator

fun MapGLRenderer.openTilePopup() {
    if (xMouseMap == OUT_OF_BOUNDS || yMouseMap == OUT_OF_BOUNDS) {
        return
    }

    view.createAndShowTilePopup(Mouse.getX(), Display.getHeight() - Mouse.getY()) { popup ->
        val tile = selectedMap!!.getTile(xMouseMap, yMouseMap) ?: return@createAndShowTilePopup

        JMenuItem("Cut").apply {
            popup.add(this)
            addActionListener {
                History.addUndoAction(TileReplaceAction(selectedMap!!, tile))
                TileOperation.cut(selectedMap!!, tile)
                Frame.update(true)
            }
        }
        JMenuItem("Copy").apply {
            popup.add(this)
            addActionListener {
                TileOperation.copy(tile)
            }
        }
        JMenuItem("Paste").apply {
            isEnabled = TileOperation.hasTileInBuffer()
            popup.add(this)
            addActionListener {
                History.addUndoAction(TileReplaceAction(selectedMap!!, tile))
                TileOperation.paste(selectedMap!!, tile.x, tile.y)
                Frame.update(true)
            }
        }
        JMenuItem("Delete").apply {
            popup.add(this)
            addActionListener {
                History.addUndoAction(TileReplaceAction(selectedMap!!, tile))
                TileOperation.delete(selectedMap!!, tile)
                Frame.update(true)
            }
        }

        popup.add(JSeparator())

        // Tile items
        tile.tileItems.sortedWith(TileItemsPopupComparator()).forEach { tileItem ->
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
                    History.addUndoAction(DeleteTileItemAction(selectedMap!!, tileItem))
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

private class TileItemsPopupComparator : Comparator<TileItem> {
    override fun compare(o1: TileItem, o2: TileItem): Int {
        return if (o1.isType(TYPE_AREA)) -1
        else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_OBJ)) 0
        else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_TURF)) -1
        else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_AREA)) 1
        else 1
    }
}
