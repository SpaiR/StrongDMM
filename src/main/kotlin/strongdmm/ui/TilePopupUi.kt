package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.VAR_NAME
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.*
import strongdmm.event.type.ui.EventEditVarsDialogUi
import strongdmm.event.type.ui.EventTilePopupUi
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
    private var activeTileItem: TileItem? = null

    init {
        consumeEvent(EventTilePopupUi.Open::class.java, ::handleOpen)
        consumeEvent(EventTilePopupUi.Close::class.java, ::handleClose)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(EventGlobal.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
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
                menuItem("Cut", shortcut = "Ctrl+X", block = ::doCut)
                menuItem("Copy", shortcut = "Ctrl+C", block = ::doCopy)
                menuItem("Paste", shortcut = "Ctrl+V", block = ::doPaste)
                menuItem("Delete", shortcut = "Delete", block = ::doDelete)
                menuItem("Deselect All", shortcut = "Esc", block = ::doDeselectAll)
                separator()
                showTileItems(tile)
            }
        }
    }

    private fun showTileItems(tile: Tile) {
        tile.area?.let { area -> showTileItemRow(tile, area.value, area.index) }
        tile.mobs.forEach { mob -> showTileItemRow(tile, mob.value, mob.index) }
        tile.objs.forEach { obj -> showTileItemRow(tile, obj.value, obj.index) }
        tile.turf?.let { turf -> showTileItemRow(tile, turf.value, turf.index) }
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
        menuItem("Move To Top##move_to_top_$tileItemIdx", enabled = (tileItem.isType(TYPE_OBJ) || tileItem.isType(TYPE_MOB))) {
            sendEvent(
                EventActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.moveToTop(tileItem, tileItemIdx)
                }
            ))

            sendEvent(EventFrameController.Refresh())
        }

        menuItem("Move To Bottom##move_to_bottom_$tileItemIdx", enabled = (tileItem.isType(TYPE_OBJ) || tileItem.isType(TYPE_MOB))) {
            sendEvent(
                EventActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.moveToBottom(tileItem, tileItemIdx)
                }
            ))

            sendEvent(EventFrameController.Refresh())
        }

        separator()

        menuItem("Make Active Object##make_active_object_$tileItemIdx", shortcut = "Shift+LMB") {
            sendEvent(EventTileItemController.ChangeActive(tileItem))
        }

        menuItem("Edit...##edit_variables_$tileItemIdx", shortcut = "Shift+RMB") {
            sendEvent(EventEditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
        }

        menuItem(
            "Replace With Active Object##replace_with_active_object_$tileItemIdx",
            shortcut = "Ctrl+Shift+LMB",
            enabled = (activeTileItem?.isSameType(tileItem) ?: false)
        ) {
            activeTileItem?.let { activeTileItem ->
                sendEvent(
                    EventActionController.AddAction(
                    ReplaceTileAction(tile) {
                        tile.replaceTileItem(tileItemIdx, activeTileItem)
                    }
                ))

                sendEvent(EventFrameController.Refresh())
            }
        }

        menuItem("Delete##delete_object_$tileItemIdx", shortcut = "Ctrl+Shift+RMB") {
            sendEvent(
                EventActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItemIdx)
                }
            ))

            sendEvent(EventFrameController.Refresh())
        }
    }

    private fun doCut() {
        sendEvent(EventClipboardController.Cut())
    }


    private fun doCopy() {
        sendEvent(EventClipboardController.Copy())
    }

    private fun doPaste() {
        sendEvent(EventClipboardController.Paste())
    }

    private fun doDelete() {
        sendEvent(EventMapModifierController.DeleteActiveAreaTileItems())
    }

    private fun doDeselectAll() {
        sendEvent(EventToolsController.Reset())
    }

    private fun handleOpen(event: Event<Tile, Unit>) {
        currentTile = event.body
        isDoOpen = true
    }

    private fun handleClose() {
        currentTile = null
    }

    private fun handleEnvironmentReset() {
        currentTile = null
    }

    private fun handleOpenedMapClosed() {
        currentTile = null
    }

    private fun handleActiveTileItemChanged(event: Event<TileItem?, Unit>) {
        activeTileItem = event.body
    }
}
