package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.history.MultipleAction
import io.github.spair.strongdmm.logic.history.TileReplaceAction
import io.github.spair.strongdmm.logic.map.TileItem
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

class PickTileSelect : TileSelect {

    private var mode: TileSelect = PickMode()

    private var x1 = 0
    private var y1 = 0
    private var x2 = 0
    private var y2 = 0

    private val selectedCoords = mutableSetOf<CoordPoint>()

    override fun onStart(x: Int, y: Int) {
        if (selectedCoords.isNotEmpty() && !selectedCoords.contains(CoordPoint(x, y))) {
            mode = PickMode()
            selectedCoords.clear()
            x1 = 0
            y1 = 0
            x2 = 0
            y2 = 0
        }

        mode.onStart(x, y)
    }

    override fun onAdd(x: Int, y: Int) {
        mode.onAdd(x, y)

        selectedCoords.clear()
        for (xS in x1..x2) {
            for (yS in y1..y2) {
                selectedCoords.add(CoordPoint(xS, yS))
            }
        }
    }

    override fun onStop() = mode.onStop()
    override fun isEmpty() = x1 == 0 || y1 == 0 || x2 == 0 || y2 == 0

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

    private inner class PickMode : TileSelect {

        private var xStart = 0
        private var yStart = 0

        override fun onStart(x: Int, y: Int) {
            xStart = x
            yStart = y
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
            mode = ManipulateMode()
        }

        override fun isEmpty() = throw UnsupportedOperationException()
        override fun render(iconSize: Int) = throw UnsupportedOperationException()
    }

    private inner class ManipulateMode : TileSelect {

        private var xClickStart = 0
        private var yClickStart = 0

        private var selectedTiles = mutableListOf<Pair<CoordPoint, List<TileItem>>>()
        private var previousTiles = mutableListOf<Pair<CoordPoint, List<TileItem>>>()

        override fun onStart(x: Int, y: Int) {
            xClickStart = x
            yClickStart = y

            val map = MapView.getSelectedMap()!!

            selectedCoords.forEach {
                val tile = map.getTile(it.x, it.y)!!
                selectedTiles.add(Pair(it, tile.getTileItems()))
                tile.clearTile()
            }
        }

        override fun onAdd(x: Int, y: Int) {
            val map = MapView.getSelectedMap()!!

            val xShift = x - xClickStart
            val yShift = y - yClickStart

            with(previousTiles) {
                forEach { (coord, tileItems) ->
                    map.getTile(coord.x, coord.y)!!.replaceTileItems(tileItems)
                }
                clear()
            }

            var xMin = Int.MAX_VALUE
            var yMin = Int.MAX_VALUE
            var xMax = Int.MIN_VALUE
            var yMax = Int.MIN_VALUE

            selectedTiles.forEach { (coord, selectedTileItems) ->
                val newX = coord.x + xShift
                val newY = coord.y + yShift
                val newTile = map.getTile(newX, newY)!!

                xMin = min(xMin, newX)
                yMin = min(yMin, newY)
                xMax = max(xMax, newX)
                yMax = max(yMax, newY)

                previousTiles.add(Pair(CoordPoint(newX, newY), newTile.getTileItems()))

                newTile.replaceTileItems(selectedTileItems)
            }

            x1 = xMin
            y1 = yMin
            x2 = xMax
            y2 = yMax

            Frame.update(true)
        }

        override fun onStop() {
            val reverseActions = mutableListOf<TileReplaceAction>()
            val map = MapView.getSelectedMap()!!

            previousTiles.forEach { (coord, tileItems) ->
                reverseActions.add(TileReplaceAction(map, coord.x, coord.y, tileItems))
            }
            selectedTiles.forEach { (coord, tileItems) ->
                reverseActions.add(TileReplaceAction(map, coord.x, coord.y, tileItems))
            }

            History.addUndoAction(MultipleAction(reverseActions))

            selectedTiles.clear()
        }

        override fun isEmpty() = throw UnsupportedOperationException()
        override fun render(iconSize: Int) = throw UnsupportedOperationException()
    }
}
