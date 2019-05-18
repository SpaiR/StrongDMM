package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.history.*
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.TileItem
import org.lwjgl.opengl.GL11.*

class AddTileSelect : TileSelect {

    private val coordsBuffer = mutableSetOf<CoordPoint>()
    private val reverseActions = mutableListOf<Undoable>()

    override fun onAdd(map: Dmm, x: Int, y: Int) {
        if (!coordsBuffer.add(CoordPoint(x, y))) {
            return
        }

        InstanceListView.selectedInstance?.let {
            val tileItem = TileItem.fromInstance(it, x, y)
            reverseActions.add(map.placeTileItemWithUndoable(tileItem))
            Frame.update(true)
        } ?: Frame.update()
    }

    override fun onStop() {
        History.addUndoAction(MultipleAction(reverseActions.toList()))
        reverseActions.clear()
        coordsBuffer.clear()
        Frame.update()
    }

    override fun isEmpty() = coordsBuffer.isEmpty()

    override fun render(iconSize: Int) {
        if (coordsBuffer.isEmpty()) {
            return
        }

        glColor3f(1f, 1f, 1f)

        coordsBuffer.forEach { coordPoint ->
            val xPos = (coordPoint.x - 1) * iconSize
            val yPos = (coordPoint.y - 1) * iconSize


            glBegin(GL_LINE_LOOP)
            run {
                glRecti(xPos, yPos, xPos + iconSize, yPos + iconSize)
                glVertex2i(xPos, yPos)
                glVertex2i(xPos + iconSize, yPos)
                glVertex2i(xPos + iconSize, yPos + iconSize)
                glVertex2i(xPos, yPos + iconSize)
            }
            glEnd()
        }
    }
}
