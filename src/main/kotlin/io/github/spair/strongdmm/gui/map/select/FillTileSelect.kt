package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.gui.map.input.KeyboardProcessor
import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.history.MultipleAction
import io.github.spair.strongdmm.logic.history.PlaceTileItemAction
import io.github.spair.strongdmm.logic.history.Undoable
import io.github.spair.strongdmm.logic.map.TileItem
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

class FillTileSelect : TileSelect {

    private var isDeleteMode = false

    private var xStart = 0
    private var yStart = 0

    private var x1 = 0
    private var y1 = 0
    private var x2 = 0
    private var y2 = 0

    override fun onStart(x: Int, y: Int) {
        xStart = x
        yStart = y
        isDeleteMode = KeyboardProcessor.isShiftDown()
        Frame.update()
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
            val map = MapView.getSelectedMap()!!
            val typeToRemove = when {
                isType(instance.type, TYPE_TURF) -> TYPE_TURF
                isType(instance.type, TYPE_AREA) -> TYPE_AREA
                isType(instance.type, TYPE_MOB) -> TYPE_MOB
                else -> TYPE_OBJ
            }

            val reverseActions = mutableListOf<Undoable>()

            for (x in x1..x2) {
                for (y in y1..y2) {
                    val tile = map.getTile(x, y)
                    val topmostItem = tile?.findTopmostTileItem(typeToRemove)

                    if (topmostItem != null) {
                        tile.deleteTileItem(topmostItem)
                        reverseActions.add(PlaceTileItemAction(map, topmostItem))
                    }
                }
            }

            if (reverseActions.isNotEmpty()) {
                History.addUndoAction(MultipleAction(reverseActions))
                return true
            }
        }

        return false
    }

    private fun placeSelectedInstances(): Boolean {
        val selectedInstance = InstanceListView.selectedInstance

        if (selectedInstance != null) {
            val map = MapView.getSelectedMap()!!
            val reverseActions = mutableListOf<Undoable>()

            for (x in x1..x2) {
                for (y in y1..y2) {
                    val tileItem = TileItem.fromInstance(selectedInstance, x, y)
                    reverseActions.add(map.placeTileItemWithUndoable(tileItem))
                }
            }

            History.addUndoAction(MultipleAction(reverseActions))
            return true
        }

        return false
    }

    override fun isEmpty() = xStart == 0 || yStart == 0

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
