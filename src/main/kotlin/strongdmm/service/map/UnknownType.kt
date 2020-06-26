package strongdmm.service.map

import strongdmm.byond.dmm.parser.TileObject

data class UnknownType(
    val originalTileObject: TileObject
) {
    var type: String = originalTileObject.type
    val variables: MutableList<Variable> = originalTileObject.vars?.map { Variable(it.key, it.value) }?.toMutableList() ?: mutableListOf()

    data class Variable(
        var name: String,
        var value: String
    )
}
