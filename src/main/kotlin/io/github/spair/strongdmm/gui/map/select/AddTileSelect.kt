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
import io.github.spair.strongdmm.logic.map.CoordPoint
import io.github.spair.strongdmm.logic.map.TileItem
import org.lwjgl.opengl.GL11.*

class AddTileSelect : TileSelect {

    private val coordsBuffer = mutableSetOf<CoordPoint>()
    private val reverseActions = mutableListOf<Undoable>()
    private var isDeleteMode = false

    override fun onStart(x: Int, y: Int) {
        isDeleteMode = KeyboardProcessor.isShiftDown()
        Frame.update()
    }

    override fun onAdd(x: Int, y: Int) {
        if (!coordsBuffer.add(CoordPoint(x, y))) {
            return
        }

        Frame.update(if (isDeleteMode) deleteTopmostItem(x, y) else placeSelectedInstance(x, y))
    }

    private fun deleteTopmostItem(x: Int, y: Int): Boolean {
        val map = MapView.getSelectedMap()!!
        val instance = InstanceListView.selectedInstance
        val tile = map.getTile(x, y)

        if (instance != null && tile != null) {
            val typeToRemove = when {
                isType(instance.type, TYPE_TURF) -> TYPE_TURF
                isType(instance.type, TYPE_AREA) -> TYPE_AREA
                isType(instance.type, TYPE_MOB) -> TYPE_MOB
                else -> TYPE_OBJ
            }

            val topmostItem = tile.findTopmostTileItem(typeToRemove)

            if (topmostItem != null) {
                tile.deleteTileItem(topmostItem)
                reverseActions.add(PlaceTileItemAction(map, topmostItem))
                return true
            }
        }

        return false
    }

    private fun placeSelectedInstance(x: Int, y: Int): Boolean {
        val instance = InstanceListView.selectedInstance

        if (instance != null) {
            val map = MapView.getSelectedMap()!!
            val tileItem = TileItem.fromInstance(instance, x, y)
            reverseActions.add(map.placeTileItemWithUndoable(tileItem))
            return true
        }

        return false
    }

    override fun onStop() {
        if (reverseActions.isNotEmpty()) {
            History.addUndoAction(MultipleAction(reverseActions.toList()))
            reverseActions.clear()
        }

        isDeleteMode = false
        coordsBuffer.clear()
        Frame.update()
    }

    override fun isEmpty() = coordsBuffer.isEmpty()

    override fun render(iconSize: Int) {
        if (isEmpty()) {
            return
        }

        glColor3f(1f, 1f, 1f)

        coordsBuffer.forEach { coordPoint ->
            val xPos = (coordPoint.x - 1) * iconSize
            val yPos = (coordPoint.y - 1) * iconSize


            glBegin(GL_LINE_LOOP)
            run {
                glVertex2i(xPos, yPos)
                glVertex2i(xPos + iconSize, yPos)
                glVertex2i(xPos + iconSize, yPos + iconSize)
                glVertex2i(xPos, yPos + iconSize)
            }
            glEnd()
        }
    }
}
