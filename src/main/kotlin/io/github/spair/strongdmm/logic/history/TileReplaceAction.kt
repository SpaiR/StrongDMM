package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.LayersManager
import io.github.spair.strongdmm.logic.map.Tile
import io.github.spair.strongdmm.logic.map.TileItem

class TileReplaceAction(
    private val map: Dmm,
    private val x: Int,
    private val y: Int,
    private val tileObjects: List<TileItem>,
    private val hiddenTypes: Array<String> = LayersManager.getHiddenTypes()
) : Undoable {

    constructor(map: Dmm, tile: Tile) : this(map, tile.x, tile.y, tile.getTileItems())

    override fun doAction(): Undoable {
        val tile = map.getTile(x, y)!!
        val reverseAction = TileReplaceAction(map, x, y, tile.getTileItems(), hiddenTypes)

        for (tileItem in tile.getTileItems()) {
            var skip = false

            for (hiddenType in hiddenTypes) {
                if (tileItem.isType(hiddenType)) {
                    skip = true
                    break
                }
            }

            if (!skip) {
                tile.deleteTileItem(tileItem)
            }
        }

        tile.placeTileItems(tileObjects)

        Frame.update(true)
        return reverseAction
    }
}
