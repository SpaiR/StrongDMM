package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.logic.history.*
import io.github.spair.strongdmm.logic.map.CoordPoint
import io.github.spair.strongdmm.logic.map.Tile
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
    private val previousTiles = mutableListOf<Pair<CoordPoint, List<TileItem>>>()

    fun getSelectedTiles(): List<Tile> {
        val map = MapView.getSelectedMap()!!
        val tiles = mutableListOf<Tile>()

        selectedCoords.forEach { (x, y) ->
            tiles.add(map.getTile(x, y)!!)
        }

        return tiles
    }

    fun selectArea(x1: Int, y1: Int, x2: Int, y2: Int) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2

        updateSelectedCoords()
        previousTiles.clear()

        val map = MapView.getSelectedMap()!!

        selectedCoords.forEach {
            map.getTile(it.x, it.y)?.let { tile ->
                previousTiles.add(CoordPoint(it.x, it.y) to tile.getTileItems())
            }
        }

        onStop()
        Frame.update()
    }

    private fun updateSelectedCoords() {
        selectedCoords.clear()
        for (xS in x1..x2) {
            for (yS in y1..y2) {
                selectedCoords.add(CoordPoint(xS, yS))
            }
        }
    }

    override fun onStart(x: Int, y: Int) {
        if (selectedCoords.isNotEmpty() && !selectedCoords.contains(CoordPoint(x, y))) {
            mode = PickMode()
            previousTiles.clear()
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
        updateSelectedCoords()
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

        override fun onStart(x: Int, y: Int) {
            xClickStart = x
            yClickStart = y

            val map = MapView.getSelectedMap()!!

            selectedCoords.forEach {
                map.getTile(it.x, it.y)?.let { tile ->
                    selectedTiles.add(Pair(it, tile.getTileItems()))
                    tile.clearTile()
                }
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

                map.getTile(newX, newY)?.let { newTile ->
                    xMin = min(xMin, newX)
                    yMin = min(yMin, newY)
                    xMax = max(xMax, newX)
                    yMax = max(yMax, newY)

                    previousTiles.add(Pair(CoordPoint(newX, newY), newTile.getTileItems()))

                    newTile.replaceTileItems(selectedTileItems)
                }
            }

            x1 = xMin
            y1 = yMin
            x2 = xMax
            y2 = yMax

            Frame.update(true)
        }

        override fun onStop() {
            val reverseActions = mutableListOf<Undoable>()
            val map = MapView.getSelectedMap()!!

            var x21 = Int.MAX_VALUE
            var y21 = Int.MAX_VALUE
            var x22 = Int.MIN_VALUE
            var y22 = Int.MIN_VALUE

            previousTiles.forEach { (coord, tileItems) ->
                x21 = min(x21, coord.x)
                y21 = min(y21, coord.y)
                x22 = max(x22, coord.x)
                y22 = max(y22, coord.y)

                reverseActions.add(TileReplaceAction(map, coord.x, coord.y, tileItems))
            }

            var x11 = Int.MAX_VALUE
            var y11 = Int.MAX_VALUE
            var x12 = Int.MIN_VALUE
            var y12 = Int.MIN_VALUE

            selectedTiles.forEach { (coord, tileItems) ->
                x11 = min(x11, coord.x)
                y11 = min(y11, coord.y)
                x12 = max(x12, coord.x)
                y12 = max(y12, coord.y)

                reverseActions.add(TileReplaceAction(map, coord.x, coord.y, tileItems))
            }

            reverseActions.add(PickAreaAction(x11, y11, x12, y12, x21, y21, x22, y22))
            History.addUndoAction(MultipleAction(reverseActions))

            selectedTiles.clear()
        }

        override fun isEmpty() = throw UnsupportedOperationException()
        override fun render(iconSize: Int) = throw UnsupportedOperationException()
    }
}
