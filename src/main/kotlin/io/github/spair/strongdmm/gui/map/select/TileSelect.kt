package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.logic.map.Dmm

interface TileSelect {
    fun onAdd(map: Dmm, x: Int, y: Int)
    fun onStop()
    fun isEmpty(): Boolean
    fun render(iconSize: Int)
}
