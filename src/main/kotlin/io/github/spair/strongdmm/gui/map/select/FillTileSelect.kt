package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.history.MultipleAction
import io.github.spair.strongdmm.logic.history.Undoable
import io.github.spair.strongdmm.logic.map.TileItem
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

class FillTileSelect : TileSelect {

    private var xStart = 0
    private var yStart = 0
    private var xBorder = 0
    private var yBorder = 0

    private var x1 = 0
    private var y1 = 0
    private var x2 = 0
    private var y2 = 0

    override fun onAdd(x: Int, y: Int) {
        if (isEmpty()) {
            xStart = x
            yStart = y
        }

        xBorder = x
        yBorder = y

        x1 = min(xStart, xBorder)
        y1 = min(yStart, yBorder)
        x2 = max(xStart, xBorder)
        y2 = max(yStart, yBorder)

        Frame.update()
    }

    override fun onStop() {
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
        }

        xStart = 0
        yStart = 0
        xBorder = 0
        yBorder = 0

        x1 = 0
        y1 = 0
        x2 = 0
        y2 = 0

        Frame.update(selectedInstance != null)
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
