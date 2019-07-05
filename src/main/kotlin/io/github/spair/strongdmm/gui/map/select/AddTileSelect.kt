package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.common.*
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.input.KeyboardProcessor
import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.action.MultipleAction
import io.github.spair.strongdmm.logic.action.PlaceTileItemAction
import io.github.spair.strongdmm.logic.action.Undoable
import io.github.spair.strongdmm.logic.map.CoordPoint
import io.github.spair.strongdmm.logic.map.TileItemProvider
import org.lwjgl.opengl.GL11.*

class AddTileSelect : TileSelect {

    private val coordsBuffer: MutableSet<CoordPoint> = mutableSetOf()
    private val reverseActions: MutableList<Undoable> = mutableListOf()
    private var isDeleteMode: Boolean = false

    override fun onStart(x: Int, y: Int) {
        isDeleteMode = KeyboardProcessor.isShiftDown()
        onAdd(x, y)
    }

    override fun onAdd(x: Int, y: Int) {
        if (!coordsBuffer.add(CoordPoint(x, y))) {
            return
        }

        Frame.update(if (isDeleteMode) deleteTopmostItem(x, y) else placeSelectedInstance(x, y))
    }

    private fun deleteTopmostItem(x: Int, y: Int): Boolean {
        val instance = InstanceListView.selectedInstance
        val tile = getSelectedMap().getTile(x, y)

        if (instance != null && tile != null) {
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

            val topmostItem = tile.findTopmostTileItem(typeToRemove)

            if (topmostItem != null) {
                tile.deleteTileItem(topmostItem)
                reverseActions.add(PlaceTileItemAction(getSelectedMap(), tile.x, tile.y, topmostItem.id))
                return true
            }
        }

        return false
    }

    private fun placeSelectedInstance(x: Int, y: Int): Boolean {
        val instance = InstanceListView.selectedInstance

        if (instance != null) {
            val tileItem = TileItemProvider.getOrCreate(instance.type, instance.customVars)
            reverseActions.add(getSelectedMap().placeTileItemWithUndoable(x, y, tileItem))
            return true
        }

        return false
    }

    override fun onStop() {
        if (reverseActions.isNotEmpty()) {
            ActionController.addUndoAction(MultipleAction(reverseActions.toList()), false)
            reverseActions.clear()
        }

        isDeleteMode = false
        coordsBuffer.clear()
        Frame.update()
    }

    override fun isEmpty(): Boolean = coordsBuffer.isEmpty()

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
