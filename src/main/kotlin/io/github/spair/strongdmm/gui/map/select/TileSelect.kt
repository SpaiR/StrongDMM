package io.github.spair.strongdmm.gui.map.select

import io.github.spair.strongdmm.gui.map.MapView
import io.github.spair.strongdmm.logic.map.Dmm

interface TileSelect {

    fun getSelectedMap(): Dmm = MapView.getSelectedDmm()!!

    fun onStart(x: Int, y: Int)
    fun onAdd(x: Int, y: Int)
    fun onStop()
    fun isEmpty(): Boolean
    fun render(iconSize: Int)
}
