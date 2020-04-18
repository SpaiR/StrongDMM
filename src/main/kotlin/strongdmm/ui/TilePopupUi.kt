package strongdmm.ui

import gnu.trove.map.hash.TIntObjectHashMap
import imgui.ImGui.*
import strongdmm.byond.*
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.ActionStatus
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.controller.preferences.NudgeMode
import strongdmm.controller.preferences.Preferences
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.*
import strongdmm.event.type.ui.TriggerEditVarsDialogUi
import strongdmm.event.type.ui.TriggerObjectPanelUi
import strongdmm.event.type.ui.TriggerTilePopupUi
import strongdmm.util.extension.getOrPut
import strongdmm.util.imgui.menu
import strongdmm.util.imgui.menuItem
import strongdmm.util.imgui.popup

class TilePopupUi : EventConsumer, EventSender {
    companion object {
        private const val ICON_SIZE: Float = 16f
        private const val POPUP_ID: String = "tile_popup"
    }

    private lateinit var providedPreferences: Preferences

    private var isDoOpen: Boolean = false
    private var currentTile: Tile? = null
    private var activeTileItem: TileItem? = null

    private var isUndoEnabled: Boolean = false
    private var isRedoEnabled: Boolean = false

    private val pixelXNudgeArrays: TIntObjectHashMap<Pair<Int, IntArray>> = TIntObjectHashMap() // initial+current
    private val pixelYNudgeArrays: TIntObjectHashMap<Pair<Int, IntArray>> = TIntObjectHashMap() // initial+current

    private val dirArrays: TIntObjectHashMap<Pair<Int, IntArray>> = TIntObjectHashMap() // initial+current

    init {
        consumeEvent(TriggerTilePopupUi.Open::class.java, ::handleOpen)
        consumeEvent(TriggerTilePopupUi.Close::class.java, ::handleClose)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(Reaction.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(Reaction.ActionStatusChanged::class.java, ::handleActionStatusChanged)
        consumeEvent(Provider.PreferencesControllerPreferences::class.java, ::handleProviderPreferencesControllerPreferences)
    }

    fun process() {
        currentTile?.let { tile ->
            if (isDoOpen) {
                sendEvent(Reaction.TilePopupOpened())
                openPopup(POPUP_ID)
                isDoOpen = false
            } else if (!isPopupOpen(POPUP_ID)) { // if it closed - it closed
                sendEvent(Reaction.TilePopupClosed())
                dispose()
                return
            }

            popup(POPUP_ID) {
                menuItem("Undo", shortcut = "Ctrl+Z", enabled = isUndoEnabled, block = ::doUndo)
                menuItem("Redo", shortcut = "Ctrl+Shift+Z", enabled = isRedoEnabled, block = ::doRedo)
                separator()
                menuItem("Cut", shortcut = "Ctrl+X", block = ::doCut)
                menuItem("Copy", shortcut = "Ctrl+C", block = ::doCopy)
                menuItem("Paste", shortcut = "Ctrl+V", block = ::doPaste)
                menuItem("Delete", shortcut = "Delete", block = ::doDelete)
                menuItem("Deselect All", shortcut = "Ctrl+D", block = ::doDeselectAll)
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
        if (tileItem.isType(TYPE_OBJ) || tileItem.isType(TYPE_MOB)) {
            showNudgeOption(true, tile, tileItem, tileItemIdx)
            showNudgeOption(false, tile, tileItem, tileItemIdx)

            separator()

            showDirOption(tile, tileItem, tileItemIdx)

            separator()

            menuItem("Move To Top##move_to_top_$tileItemIdx") {
                sendEvent(TriggerActionController.AddAction(
                    ReplaceTileAction(tile) {
                        tile.moveToTop(tileItem, tileItemIdx)
                    }
                ))

                sendEvent(TriggerFrameController.RefreshFrame())
            }

            menuItem("Move To Bottom##move_to_bottom_$tileItemIdx") {
                sendEvent(TriggerActionController.AddAction(
                    ReplaceTileAction(tile) {
                        tile.moveToBottom(tileItem, tileItemIdx)
                    }
                ))

                sendEvent(TriggerFrameController.RefreshFrame())
            }

            separator()
        }

        menuItem("Make Active Object##make_active_object_$tileItemIdx", shortcut = "Shift+LMB") {
            sendEvent(TriggerTileItemController.ChangeActiveTileItem(tileItem))
        }

        menuItem("Edit...##edit_variables_$tileItemIdx", shortcut = "Shift+RMB") {
            sendEvent(TriggerEditVarsDialogUi.OpenWithTile(Pair(tile, tileItemIdx)))
        }

        menuItem("Delete##delete_object_$tileItemIdx", shortcut = "Ctrl+Shift+LMB") {
            sendEvent(TriggerActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.deleteTileItem(tileItemIdx)
                }
            ))

            sendEvent(TriggerFrameController.RefreshFrame())
        }

        menuItem(
            "Replace With Active Object##replace_with_active_object_$tileItemIdx",
            shortcut = "Ctrl+Shift+RMB",
            enabled = (activeTileItem?.isSameType(tileItem) ?: false)
        ) {
            activeTileItem?.let { activeTileItem ->
                sendEvent(TriggerActionController.AddAction(
                    ReplaceTileAction(tile) {
                        tile.replaceTileItem(tileItemIdx, activeTileItem)
                    }
                ))

                sendEvent(TriggerFrameController.RefreshFrame())
            }
        }

        menuItem("Reset to Default##reset_to_default_$tileItemIdx") {
            sendEvent(TriggerActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.replaceTileItem(tileItemIdx, GlobalTileItemHolder.getOrCreate(tileItem.type))
                }
            ))

            sendEvent(TriggerFrameController.RefreshFrame())
        }
    }

    private fun showNudgeOption(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        val nudgeValue = when (providedPreferences.nudgeMode) {
            NudgeMode.PIXEL -> {
                if (isXAxis) {
                    pixelXNudgeArrays.getOrPut(tileItemIdx) { tileItem.pixelX to intArrayOf(tileItem.pixelX) }
                } else {
                    pixelYNudgeArrays.getOrPut(tileItemIdx) { tileItem.pixelY to intArrayOf(tileItem.pixelY) }
                }
            }
            NudgeMode.STEP -> {
                if (isXAxis) {
                    pixelXNudgeArrays.getOrPut(tileItemIdx) { tileItem.stepX to intArrayOf(tileItem.stepX) }
                } else {
                    pixelYNudgeArrays.getOrPut(tileItemIdx) { tileItem.stepY to intArrayOf(tileItem.stepY) }
                }
            }
        }

        val (initialValue, pixelNudge) = nudgeValue
        val axisName = if (isXAxis) "X" else "Y"

        setNextItemWidth(50f)

        if (dragInt("Nudge $axisName-axis###nudge_${axisName}_option_$tileItemIdx", pixelNudge, .25f)) {
            GlobalTileItemHolder.tmpOperation {
                tile.nudge(isXAxis, tileItem, tileItemIdx, pixelNudge[0], providedPreferences.nudgeMode)
            }

            sendEvent(TriggerFrameController.RefreshFrame())
        }

        if (isItemDeactivatedAfterEdit()) {
            GlobalTileItemHolder.tmpOperation {
                tile.nudge(isXAxis, tileItem, tileItemIdx, initialValue, providedPreferences.nudgeMode)
            }

            sendEvent(TriggerActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.nudge(isXAxis, tileItem, tileItemIdx, pixelNudge[0], providedPreferences.nudgeMode)
                }
            ))

            sendEvent(TriggerFrameController.RefreshFrame())
            sendEvent(TriggerObjectPanelUi.Update())

            // to properly create a reverse action
            if (isXAxis) {
                pixelXNudgeArrays.clear()
            } else {
                pixelYNudgeArrays.clear()
            }
        }
    }

    private fun showDirOption(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        val maxDirs = GlobalDmiHolder.getIconState(tileItem.icon, tileItem.iconState)?.dirs ?: 1

        if (maxDirs == 1) {
            textDisabled("No Dirs to Choose")
            return
        }

        val dirValue = dirArrays.getOrPut(tileItemIdx) { tileItem.dir to intArrayOf(dirToRel(tileItem.dir)) }
        val (initialValue, relDir) = dirValue
        val transformedDir = relToDir(relDir[0])

        setNextItemWidth(100f)

        if (sliderInt("Dir [${dirToStr(transformedDir)}]###dir_option_$tileItemIdx", relDir, 1, maxDirs, "$transformedDir")) {
            GlobalTileItemHolder.tmpOperation {
                tile.setDir(tileItem, tileItemIdx, relToDir(relDir[0]))
            }

            sendEvent(TriggerFrameController.RefreshFrame())
        }

        if (isItemDeactivatedAfterEdit()) {
            GlobalTileItemHolder.tmpOperation {
                tile.setDir(tileItem, tileItemIdx, initialValue)
            }

            sendEvent(TriggerActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.setDir(tileItem, tileItemIdx, relToDir(relDir[0]))
                }
            ))

            sendEvent(TriggerFrameController.RefreshFrame())
            sendEvent(TriggerObjectPanelUi.Update())

            dirArrays.clear() // to properly create a reverse action
        }
    }

    private fun doUndo() {
        sendEvent(TriggerActionController.UndoAction())
    }

    private fun doRedo() {
        sendEvent(TriggerActionController.RedoAction())
    }

    private fun doCut() {
        sendEvent(TriggerClipboardController.Cut())
    }

    private fun doCopy() {
        sendEvent(TriggerClipboardController.Copy())
    }

    private fun doPaste() {
        sendEvent(TriggerClipboardController.Paste())
    }

    private fun doDelete() {
        sendEvent(TriggerMapModifierController.DeleteTileItemsInActiveArea())
    }

    private fun doDeselectAll() {
        sendEvent(TriggerToolsController.ResetTool())
    }

    private fun dispose() {
        currentTile = null
        pixelXNudgeArrays.clear()
        pixelYNudgeArrays.clear()
        dirArrays.clear()
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

    private fun handleProviderPreferencesControllerPreferences(event: Event<Preferences, Unit>) {
        providedPreferences = event.body
    }
}
