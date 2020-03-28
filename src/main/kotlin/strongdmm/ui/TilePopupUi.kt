package strongdmm.ui

import gnu.trove.map.hash.TIntObjectHashMap
import imgui.ImGui.*
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.VAR_NAME
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.ActionStatus
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.*
import strongdmm.event.type.ui.EventEditVarsDialogUi
import strongdmm.event.type.ui.EventObjectPanelUi
import strongdmm.event.type.ui.EventTilePopupUi
import strongdmm.util.extension.getOrPut
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

    private var isUndoEnabled: Boolean = false
    private var isRedoEnabled: Boolean = false

    private val pixelXNudgeArrays: TIntObjectHashMap<Pair<Int, IntArray>> = TIntObjectHashMap()
    private val pixelYNudgeArrays: TIntObjectHashMap<Pair<Int, IntArray>> = TIntObjectHashMap()

    init {
        consumeEvent(EventTilePopupUi.Open::class.java, ::handleOpen)
        consumeEvent(EventTilePopupUi.Close::class.java, ::handleClose)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(EventGlobal.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(EventGlobal.ActionStatusChanged::class.java, ::handleActionStatusChanged)
    }

    fun process() {
        currentTile?.let { tile ->
            if (isDoOpen) {
                openPopup(POPUP_ID)
                isDoOpen = false
            } else if (!isPopupOpen(POPUP_ID)) { // if it closed - it closed
                dispose()
                return
            }

            popup(POPUP_ID, ImGuiWindowFlags.NoMove) {
                menuItem("Undo", shortcut = "Ctrl+Z", enabled = isUndoEnabled, block = ::doUndo)
                menuItem("Redo", shortcut = "Ctrl+Shift+Z", enabled = isRedoEnabled, block = ::doRedo)
                separator()
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
        tile.area?.let { area -> showTileItem(tile, area.value, area.index) }
        tile.mobs.forEach { mob -> showTileItem(tile, mob.value, mob.index) }
        tile.objs.forEach { obj -> showTileItem(tile, obj.value, obj.index) }
        tile.turf?.let { turf -> showTileItem(tile, turf.value, turf.index) }
    }

    private fun showTileItem(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        val sprite = GlobalDmiHolder.getIconSpriteOrPlaceholder(tileItem.icon, tileItem.iconState, tileItem.dir)
        val name = tileItem.getVarText(VAR_NAME)!!

        image(sprite.textureId, ICON_SIZE, ICON_SIZE, sprite.u1, sprite.v1, sprite.u2, sprite.v2, tileItem.colorR, tileItem.colorG, tileItem.colorB, 1f)
        sameLine()
        menu("$name##tile_item_row_$tileItemIdx") { showTileItemOptions(tile, tileItem, tileItemIdx) }
        sameLine()
        text("[${tileItem.type}]  ") // Two spaces in the end to make text not to overlap over the menu arrow.
    }

    private fun showTileItemOptions(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        showNudgeOption(true, tile, tileItem, tileItemIdx, pixelXNudgeArrays.getOrPut(tileItemIdx) { tileItem.pixelX to intArrayOf(tileItem.pixelX) })
        showNudgeOption(false, tile, tileItem, tileItemIdx, pixelYNudgeArrays.getOrPut(tileItemIdx) { tileItem.pixelY to intArrayOf(tileItem.pixelY) })

        separator()

        menuItem("Move To Top##move_to_top_$tileItemIdx", enabled = (tileItem.isType(TYPE_OBJ) || tileItem.isType(TYPE_MOB))) {
            sendEvent(EventActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.moveToTop(tileItem, tileItemIdx)
                }
            ))

            sendEvent(EventFrameController.RefreshFrame())
        }

        menuItem("Move To Bottom##move_to_bottom_$tileItemIdx", enabled = (tileItem.isType(TYPE_OBJ) || tileItem.isType(TYPE_MOB))) {
            sendEvent(EventActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.moveToBottom(tileItem, tileItemIdx)
                }
            ))

            sendEvent(EventFrameController.RefreshFrame())
        }

        separator()

        menuItem("Make Active Object##make_active_object_$tileItemIdx", shortcut = "Shift+LMB") {
            sendEvent(EventTileItemController.ChangeActiveTileItem(tileItem))
        }

        menuItem("Edit...##edit_variables_$tileItemIdx", shortcut = "Shift+RMB") {
            sendEvent(EventEditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
        }

        menuItem("Delete##delete_object_$tileItemIdx", shortcut = "Ctrl+Shift+LMB") {
            sendEvent(EventActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItemIdx)
                }
            ))

            sendEvent(EventFrameController.RefreshFrame())
        }

        menuItem(
            "Replace With Active Object##replace_with_active_object_$tileItemIdx",
            shortcut = "Ctrl+Shift+RMB",
            enabled = (activeTileItem?.isSameType(tileItem) ?: false)
        ) {
            activeTileItem?.let { activeTileItem ->
                sendEvent(EventActionController.AddAction(
                    ReplaceTileAction(tile) {
                        tile.replaceTileItem(tileItemIdx, activeTileItem)
                    }
                ))

                sendEvent(EventFrameController.RefreshFrame())
            }
        }
    }

    private fun showNudgeOption(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int, nudgeValue: Pair<Int, IntArray>) {
        val (initialValue, pixelNudge) = nudgeValue

        setNextItemWidth(50f)

        if (dragInt("Nudge %s-axis".format(if (isXAxis) "X" else "Y"), pixelNudge, .25f)) {
            GlobalTileItemHolder.tmpOperation {
                tile.nudge(isXAxis, tileItem, tileItemIdx, pixelNudge[0])
            }

            sendEvent(EventFrameController.RefreshFrame())
        }

        if (isItemDeactivatedAfterEdit()) {
            GlobalTileItemHolder.tmpOperation {
                tile.nudge(isXAxis, tileItem, tileItemIdx, initialValue)
            }

            sendEvent(EventActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.nudge(isXAxis, tileItem, tileItemIdx, pixelNudge[0])
                }
            ))

            sendEvent(EventFrameController.RefreshFrame())
            sendEvent(EventObjectPanelUi.Update())

            if (isXAxis) {
                pixelXNudgeArrays.clear()
            } else {
                pixelYNudgeArrays.clear()
            }
        }
    }

    private fun doUndo() {
        sendEvent(EventActionController.UndoAction())
    }

    private fun doRedo() {
        sendEvent(EventActionController.RedoAction())
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
        sendEvent(EventMapModifierController.DeleteTileItemsInActiveArea())
    }

    private fun doDeselectAll() {
        sendEvent(EventToolsController.ResetTool())
    }

    private fun dispose() {
        currentTile = null
        pixelXNudgeArrays.clear()
        pixelYNudgeArrays.clear()
    }

    private fun handleOpen(event: Event<Tile, Unit>) {
        currentTile = event.body
        isDoOpen = true
    }

    private fun handleClose() {
        dispose()
    }

    private fun handleEnvironmentReset() {
        dispose()
        activeTileItem = null
    }

    private fun handleOpenedMapClosed() {
        currentTile = null
    }

    private fun handleActiveTileItemChanged(event: Event<TileItem?, Unit>) {
        activeTileItem = event.body
    }

    private fun handleActionStatusChanged(event: Event<ActionStatus, Unit>) {
        isUndoEnabled = event.body.hasUndoAction
        isRedoEnabled = event.body.hasRedoAction
    }
}
