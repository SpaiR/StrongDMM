package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.Tile
import io.github.spair.strongdmm.logic.map.TileItem

class TileReplaceAction(private val map: Dmm) : Undoable {

    private var x: Int = 0
    private var y: Int = 0
    private lateinit var tileObjects: List<TileItem>

    constructor(map: Dmm, tile: Tile) : this(map) {
        x = tile.x
        y = tile.y
        tileObjects = tile.getTileItems()
    }

    constructor(map: Dmm, x: Int, y: Int, tileObjects: List<TileItem>) : this(map) {
        this.x = x
        this.y = y
        this.tileObjects = tileObjects.toList()
    }

    override fun doAction(): Undoable {
        val tile = map.getTile(x, y)!!
        val reverseAction = TileReplaceAction(map, tile)
        tile.replaceTileItems(tileObjects)
        Frame.update(true)
        return reverseAction
    }
}
