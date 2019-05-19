package io.github.spair.strongdmm.gui.map.select

interface TileSelect {
    fun onAdd(x: Int, y: Int)
    fun onStop()
    fun isEmpty(): Boolean
    fun render(iconSize: Int)
}
