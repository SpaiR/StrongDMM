package strongdmm.ui.tile.popup

import gnu.trove.map.hash.TIntObjectHashMap
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.preferences.Preferences

class State {
    lateinit var providedPreferences: Preferences

    var isDoOpen: Boolean = false
    var currentTile: Tile? = null

    var selectedTileItem: TileItem? = null

    var isUndoEnabled: Boolean = false
    var isRedoEnabled: Boolean = false

    // Those are used to store values while nudging
    val pixelXNudgeArrays: TIntObjectHashMap<Pair<Int, IntArray>> = TIntObjectHashMap() // initial+current
    val pixelYNudgeArrays: TIntObjectHashMap<Pair<Int, IntArray>> = TIntObjectHashMap() // initial+current

    // Array to store values while changing a dir
    val dirArrays: TIntObjectHashMap<Pair<Int, IntArray>> = TIntObjectHashMap() // initial+current
}
