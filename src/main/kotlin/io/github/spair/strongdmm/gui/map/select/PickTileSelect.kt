package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.action.MultipleAction
import io.github.spair.strongdmm.logic.action.TileReplaceAction
import io.github.spair.strongdmm.logic.action.Undoable
import io.github.spair.strongdmm.logic.map.CoordPoint
import io.github.spair.strongdmm.logic.map.Tile
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

class PickTileSelect : TileSelect {

    private var mode: TileSelect = PickMode()

    private var x1: Int = 0
    private var y1: Int = 0
    private var x2: Int = 0
    private var y2: Int = 0

    private val selectedCoords: MutableSet<CoordPoint> = mutableSetOf()
    private val previousTiles: MutableMap<CoordPoint, TileData> = mutableMapOf()

    fun getSelectedTiles(): List<Tile> {
        val map = getSelectedMap()
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

        val map = getSelectedMap()

        selectedCoords.forEach {
            map.getTile(it.x, it.y)?.let { tile ->
                previousTiles[CoordPoint(it.x, it.y)] = TileData(tile)
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

        override fun isEmpty(): Boolean = throw UnsupportedOperationException()
        override fun render(iconSize: Int) = throw UnsupportedOperationException()
    }

    private inner class ManipulateMode : TileSelect {

        private var xClickStart: Int = 0
        private var yClickStart: Int = 0

        private var selectedTiles: MutableMap<CoordPoint, TileData> = mutableMapOf()

        override fun onStart(x: Int, y: Int) {
            xClickStart = x
            yClickStart = y

            val map = getSelectedMap()

            selectedCoords.forEach {
                map.getTile(it.x, it.y)?.let { tile ->
                    selectedTiles[it] = TileData(tile)
                    tile.deleteVisibleTileItems()
                }
            }
        }

        override fun onAdd(x: Int, y: Int) {
            val map = getSelectedMap()
            val xShift = x - xClickStart
            val yShift = y - yClickStart

            previousTiles.forEach { (coord, tileData) ->
                map.getTile(coord.x, coord.y)?.replaceOnlyVisibleTileItemsByIDs(tileData.visibleTileItemsIDs)
            }
            previousTiles.clear()

            var xMin = Int.MAX_VALUE
            var yMin = Int.MAX_VALUE
            var xMax = Int.MIN_VALUE
            var yMax = Int.MIN_VALUE

            selectedTiles.forEach { (coord, tileData) ->
                val newX = coord.x + xShift
                val newY = coord.y + yShift

                map.getTile(newX, newY)?.let { newTile ->
                    xMin = min(xMin, newX)
                    yMin = min(yMin, newY)
                    xMax = max(xMax, newX)
                    yMax = max(yMax, newY)

                    previousTiles[CoordPoint(newX, newY)] = TileData(newTile)
                    newTile.replaceOnlyVisibleTileItemsByIDs(tileData.visibleTileItemsIDs)
                }
            }

            x1 = xMin
            y1 = yMin
            x2 = xMax
            y2 = yMax

            Frame.update(true)
        }

        override fun onStop() {
            val map = getSelectedMap()
            val reverseActions = mutableListOf<Undoable>()

            selectedTiles.forEach { (coord, tileData) ->
                map.getTile(coord.x, coord.y)?.let { tile ->
                    reverseActions.add(
                        TileReplaceAction(map, coord.x, coord.y, tileData.allTileItemsIDs, tile.getTileItemsIDs())
                    )
                }
            }

            previousTiles.forEach { (coord, tileData) ->
                if (!selectedTiles.containsKey(coord)) {
                    map.getTile(coord.x, coord.y)?.let { tile ->
                        reverseActions.add(TileReplaceAction(map, coord.x, coord.y, tileData.allTileItemsIDs, tile.getTileItemsIDs()))
                    }
                }
            }

            ActionController.addUndoAction(MultipleAction(reverseActions))
            selectedTiles.clear()
        }

        override fun isEmpty(): Boolean = throw UnsupportedOperationException()
        override fun render(iconSize: Int) = throw UnsupportedOperationException()
    }

    private class TileData(tile: Tile) {
        val allTileItemsIDs: IntArray = tile.getTileItemsIDs()
        val visibleTileItemsIDs = tile.getVisibleTileItemsIDs()
    }
}
