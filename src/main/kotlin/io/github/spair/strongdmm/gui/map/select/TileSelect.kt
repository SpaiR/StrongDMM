package io.github.spair.strongdmm.gui.map.select

interface TileSelect {
    fun onStart(x: Int, y: Int)
    fun onAdd(x: Int, y: Int)
    fun onStop()
    fun isEmpty(): Boolean
    fun render(iconSize: Int)
}
