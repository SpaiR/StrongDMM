package io.github.spair.strongdmm.logic.map.extension

import io.github.spair.dmm.io.DmmData
import io.github.spair.dmm.io.TileContent
import io.github.spair.dmm.io.TileLocation

fun DmmData.deleteTile(x: Int, y: Int) {
    removeTileContentByLocation(TileLocation.of(x, y))
}

fun DmmData.addTile(x: Int, y: Int, tileContent: TileContent) {
    addTileContentByLocation(TileLocation.of(x, y), tileContent)
}
