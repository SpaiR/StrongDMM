package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.common.*
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.input.KeyboardProcessor
import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.action.MultipleAction
import io.github.spair.strongdmm.logic.action.PlaceTileItemAction
import io.github.spair.strongdmm.logic.action.Undoable
import io.github.spair.strongdmm.logic.map.TileItemProvider
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

class FillTileSelect : TileSelect {

    private var isDeleteMode: Boolean = false

    private var xStart: Int = 0
    private var yStart: Int = 0

    private var x1: Int = 0
    private var y1: Int = 0
    private var x2: Int = 0
    private var y2: Int = 0

    override fun onStart(x: Int, y: Int) {
        xStart = x
        yStart = y
        isDeleteMode = KeyboardProcessor.isShiftDown()
        onAdd(x, y)
    }

    override fun onAdd(x: Int, y: Int) {
        x1 = min(xStart, x)
        y1 = min(yStart, y)
        x2 = max(xStart, x)
        y2 = max(yStart, y)
        Frame.update()
    }

    override fun onStop() {
        val isForced = if (isDeleteMode) deleteTopmostItems() else placeSelectedInstances()

        isDeleteMode = false

        xStart = 0
        yStart = 0

        x1 = 0
        y1 = 0
        x2 = 0
        y2 = 0

        Frame.update(isForced)
    }

    private fun deleteTopmostItems(): Boolean {
        val instance = InstanceListView.selectedInstance

        if (instance != null) {
            val typeToRemove = when {
                isType(
                    instance.type,
                    TYPE_TURF
                ) -> TYPE_TURF
                isType(
                    instance.type,
                    TYPE_AREA
                ) -> TYPE_AREA
                isType(
                    instance.type,
                    TYPE_MOB
                ) -> TYPE_MOB
                else -> TYPE_OBJ
            }

            val reverseActions = mutableListOf<Undoable>()
            val map = getSelectedMap()

            for (x in x1..x2) {
                for (y in y1..y2) {
                    val tile = map.getTile(x, y)
                    val topmostItem = tile?.findTopmostTileItem(typeToRemove)

                    if (topmostItem != null) {
                        tile.deleteTileItem(topmostItem)
                        reverseActions.add(PlaceTileItemAction(map, tile.x, tile.y, topmostItem.id))
                    }
                }
            }

            if (reverseActions.isNotEmpty()) {
                ActionController.addUndoAction(MultipleAction(reverseActions))
                return true
            }
        }

        return false
    }

    private fun placeSelectedInstances(): Boolean {
        val selectedInstance = InstanceListView.selectedInstance

        if (selectedInstance != null) {
            val reverseActions = mutableListOf<Undoable>()
            val map = getSelectedMap()

            for (x in x1..x2) {
                for (y in y1..y2) {
                    val tileItem = TileItemProvider.getOrCreate(selectedInstance.type, selectedInstance.customVars)
                    reverseActions.add(map.placeTileItemWithUndoable(x, y, tileItem))
                }
            }

            ActionController.addUndoAction(MultipleAction(reverseActions), false)
            return true
        }

        return false
    }

    override fun isEmpty(): Boolean = xStart == 0 || yStart == 0

    override fun render(iconSize: Int) {
        if (isEmpty()) {
            return
        }

        GL11.glColor3f(1f, 1f, 1f)

        val x1 = (x1 - 1) * iconSize
        val y1 = (y1 - 1) * iconSize
        val x2 = (x2 - 1) * iconSize
        val y2 = (y2 - 1) * iconSize

        GL11.glBegin(GL11.GL_LINE_LOOP)
        run {
            GL11.glVertex2i(x1, y1)
            GL11.glVertex2i(x2 + iconSize, y1)
            GL11.glVertex2i(x2 + iconSize, y2 + iconSize)
            GL11.glVertex2i(x1, y2 + iconSize)
        }
        GL11.glEnd()
    }
}
