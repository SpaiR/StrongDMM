package strongdmm.ui.dialog.edit_vars

import imgui.type.ImBoolean
import imgui.type.ImString
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.ui.dialog.edit_vars.model.Variable
import java.util.*

class State {
    companion object {
        const val FILTER_BUFFER: Int = 10
    }

    var windowId: Long = 0 // To ensure every window will be unique.
    var isFistOpen: Boolean = true
    var isDoUpdatePinnedVariables: Boolean = false

    var currentTileItem: TileItem? = null // We can open edit menu with a tile item.
    var newTileItem: TileItem? = null // Stores a new instance of the modified tile item.
    var newTileItemEvent: Event<TileItem, TileItem>? = null // Stores an event to reply if we've opened the window with the tile item.

    var currentTile: Tile? = null // We can open edit menu with with a tile. If opened with the tile, then changes will be applied to a map.
    var initialTileItemsId: LongArray? = null // Used to restore tile state if we didn't save our modified vars
    var currentTileItemIndex: Int = 0 // This index is an item index inside of a Tile objects list

    var currentEditVar: Variable? = null

    val variables: MutableList<Variable> = mutableListOf()
    val variablesByType: TreeMap<String, List<Variable>> = TreeMap(Comparator.reverseOrder())
    val pinnedVariables: MutableList<Variable> = mutableListOf()

    // Variables filter buffer, resizable string
    val varsFilter: ImString = ImString(FILTER_BUFFER).apply { inputData.isResizable = true }
    val typesFilter: ImString = ImString(FILTER_BUFFER).apply { inputData.isResizable = true }

    val isShowModifiedVars: ImBoolean = ImBoolean(false)
    val isShowVarsByType: ImBoolean = ImBoolean(false)
    var variableInputFocused: Boolean = false // On first var select we will focus its input. Var is to check if we've already did it
}
