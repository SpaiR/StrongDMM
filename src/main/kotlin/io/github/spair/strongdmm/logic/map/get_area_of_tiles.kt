package io.github.spair.strongdmm.logic.map

import kotlin.math.max
import kotlin.math.min

fun getAreaOfTiles(tiles: Collection<Tile>): CoordArea {
    var x1 = Int.MAX_VALUE
    var y1 = Int.MAX_VALUE
    var x2 = Int.MIN_VALUE
    var y2 = Int.MIN_VALUE

    tiles.forEach { tile ->
        x1 = min(x1, tile.x)
        y1 = min(y1, tile.y)
        x2 = max(x2, tile.x)
        y2 = max(y2, tile.y)
    }

    return CoordArea(x1, y1, x2, y2)
}

fun getAreaOfTiles(tilesCoords: Set<CoordPoint>): CoordArea {
    var x1 = Int.MAX_VALUE
    var y1 = Int.MAX_VALUE
    var x2 = Int.MIN_VALUE
    var y2 = Int.MIN_VALUE

    tilesCoords.forEach {
        x1 = min(x1, it.x)
        y1 = min(y1, it.y)
        x2 = max(x2, it.x)
        y2 = max(y2, it.y)
    }

    return CoordArea(x1, y1, x2, y2)
}
