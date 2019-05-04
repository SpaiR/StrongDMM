package io.github.spair.strongdmm.gui.mapcanvas

import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.edit.ViewVariablesDialog
import io.github.spair.strongdmm.gui.instancelist.InstanceListController
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.dme.VAR_NAME
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.map.OUT_OF_BOUNDS
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import javax.swing.JMenu
import javax.swing.JMenuItem

private val dmiProvider by diInstance<DmiProvider>()
private val instanceListController by diInstance<InstanceListController>()
private val objectTreeView by diInstance<ObjectTreeView>()

fun MapCanvasController.openTilePopup() {
    if (xMouseMap == OUT_OF_BOUNDS || yMouseMap == OUT_OF_BOUNDS) {
        return
    }

    view.createAndShowTilePopup(Mouse.getX(), Display.getHeight() - Mouse.getY()) { popup ->
        selectedMap!!.getTile(xMouseMap, yMouseMap)!!.tileItems.forEach { tileItem ->
            val menu = JMenu("${tileItem.getVarText(VAR_NAME)}  [${tileItem.type}]").apply { popup.add(this) }

            dmiProvider.getDmi(tileItem.icon)?.let { dmi ->
                dmi.getIconState(tileItem.iconState)?.let { iconState ->
                    menu.icon = iconState.getIconSprite(tileItem.dir).scaledIcon
                }
            }

            menu.add(JMenuItem("Make Active Object").apply {
                addActionListener {
                    objectTreeView.findAndSelectPath(tileItem.type, true)
                }
            })

            menu.add(JMenuItem("View Variables")).apply {
                addActionListener {
                    if (ViewVariablesDialog(tileItem).open()) {
                        Frame.update(true)
                        instanceListController.updateSelectedInstanceInfo()
                    }
                }
            }
        }
    }
}
