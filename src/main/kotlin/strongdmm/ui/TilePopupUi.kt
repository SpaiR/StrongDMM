package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.VAR_NAME
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.Tile.Companion.TILE_ITEM_IDX_AREA
import strongdmm.byond.dmm.Tile.Companion.TILE_ITEM_IDX_TURF
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.imgui.menu
import strongdmm.util.imgui.menuItem
import strongdmm.util.imgui.popup

class TilePopupUi : EventConsumer, EventSender {
    companion object {
        private const val ICON_SIZE: Float = 16f
        private const val POPUP_ID: String = "tile_popup"
    }

    private var isDoOpen: Boolean = false
    private var currentTile: Tile? = null

    init {
        consumeEvent(Event.TilePopupUi.Open::class.java, ::handleOpen)
        consumeEvent(Event.TilePopupUi.Close::class.java, ::handleClose)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
    }

    fun process() {
        currentTile?.let { tile ->
            if (isDoOpen) {
                openPopup(POPUP_ID)
                isDoOpen = false
            } else if (!isPopupOpen(POPUP_ID)) { // if it closed - it closed
                currentTile = null
            }

            popup(POPUP_ID, ImGuiWindowFlags.NoMove) {
                showTileItems(tile)
            }
        }
    }

    private fun showTileItems(tile: Tile) {
        tile.area?.let { area -> showTileItemRow(tile, area, TILE_ITEM_IDX_AREA) }
        tile.mobs.forEach { mob -> showTileItemRow(tile, mob.value, mob.index) }
        tile.objs.forEach { obj -> showTileItemRow(tile, obj.value, obj.index) }
        tile.turf?.let { turf -> showTileItemRow(tile, turf, TILE_ITEM_IDX_TURF) }
    }

    private fun showTileItemRow(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        val sprite = GlobalDmiHolder.getIconSpriteOrPlaceholder(tileItem.icon, tileItem.iconState, tileItem.dir)
        val name = tileItem.getVarText(VAR_NAME)!!

        image(sprite.textureId, ICON_SIZE, ICON_SIZE, sprite.u1, sprite.v1, sprite.u2, sprite.v2, tileItem.colorR, tileItem.colorG, tileItem.colorB, 1f)
        sameLine()
        menu("$name##tile_item_row_$tileItemIdx") { showTileItemOptions(tile, tileItem, tileItemIdx) }
        sameLine()
        text("[${tileItem.type}]  ") // Two spaces in the end to make text not to overlap over the menu arrow.
    }

    private fun showTileItemOptions(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        if (tileItemIdx != TILE_ITEM_IDX_AREA && tileItemIdx != TILE_ITEM_IDX_TURF) {
            menuItem("Move To Top##move_to_top_$tileItemIdx") {
                sendEvent(Event.ActionController.AddAction(
                    ReplaceTileAction(tile) {
                        tile.moveToTop(tileItem.isType(TYPE_MOB), tileItemIdx)
                    }
                ))

                sendEvent(Event.Global.RefreshFrame())
            }
            menuItem("Move To Bottom##move_to_bottom_$tileItemIdx") {
                sendEvent(Event.ActionController.AddAction(
                    ReplaceTileAction(tile) {
                        tile.moveToBottom(tileItem.isType(TYPE_MOB), tileItemIdx)
                    }
                ))

                sendEvent(Event.Global.RefreshFrame())
            }

            separator()
        }

        menuItem("Make Active Object##make_active_object_$tileItemIdx", shortcut = "Shift+LMB") {
            sendEvent(Event.Global.SwitchSelectedTileItem(tileItem))
        }

        menuItem("Edit...##edit_variables_$tileItemIdx", shortcut = "Shift+RMB") {
            sendEvent(Event.EditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
        }
    }

    private fun handleOpen(event: Event<Tile, Unit>) {
        currentTile = event.body
        isDoOpen = true
    }

    private fun handleClose() {
        currentTile = null
    }

    private fun handleResetEnvironment() {
        currentTile = null
    }

    private fun handleCloseMap() {
        currentTile = null
    }
}
