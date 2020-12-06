package strongdmm.ui.tilepopup

import gnu.trove.map.hash.TIntObjectHashMap
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.service.dmi.DmiCache
import strongdmm.service.preferences.Preferences

class State {
    lateinit var providedPreferences: Preferences
    lateinit var providedDmiCache: DmiCache

    var isDoOpen: Boolean = false
    var isDisposed: Boolean = true

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
