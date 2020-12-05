package strongdmm.ui.tilepopup

import imgui.ImGui
import strongdmm.byond.*
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.util.imgui.imGuiMenu
import strongdmm.util.imgui.imGuiMenuItem
import strongdmm.util.imgui.imGuiPopup
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private const val POPUP_ID: String = "tile_popup"
        private val iconSize: Float
            get() = 16f * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        state.currentTile?.let { tile ->
            if (state.isDoOpen) {
                ImGui.openPopup(POPUP_ID)
                state.isDoOpen = false
            } else if (!ImGui.isPopupOpen(POPUP_ID)) { // if it closed - it closed
                viewController.dispose()
                return
            }

            imGuiPopup(POPUP_ID) {
                imGuiMenuItem("Undo", shortcut = "Ctrl+Z", enabled = state.isUndoEnabled, block = viewController::doUndo)
                imGuiMenuItem("Redo", shortcut = "Ctrl+Shift+Z", enabled = state.isRedoEnabled, block = viewController::doRedo)
                ImGui.separator()
                imGuiMenuItem("Cut", shortcut = "Ctrl+X", block = viewController::doCut)
                imGuiMenuItem("Copy", shortcut = "Ctrl+C", block = viewController::doCopy)
                imGuiMenuItem("Paste", shortcut = "Ctrl+V", block = viewController::doPaste)
                imGuiMenuItem("Delete", shortcut = "Delete", block = viewController::doDelete)
                imGuiMenuItem("Deselect All", shortcut = "Ctrl+D", block = viewController::doDeselectAll)
                ImGui.separator()
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

        ImGui.image(sprite.textureId, iconSize, iconSize, sprite.u1, sprite.v1, sprite.u2, sprite.v2, tileItem.colorR, tileItem.colorG, tileItem.colorB, 1f)
        ImGui.sameLine()
        imGuiMenu("${tileItem.name}##tile_item_row_$tileItemIdx") {
            showTileItemOptions(tile, tileItem, tileItemIdx)
        }
        ImGui.sameLine()
        ImGui.text("[${tileItem.type}]\t\t") // Tabs to make text not to overlap over the menu arrow.
    }

    private fun showTileItemOptions(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        val isMovable = tileItem.isType(TYPE_OBJ) || tileItem.isType(TYPE_MOB)

        if (isMovable) {
            showNudgeOption(true, tile, tileItem, tileItemIdx)
            showNudgeOption(false, tile, tileItem, tileItemIdx)
            ImGui.separator()
        }

        if (!tileItem.isType(TYPE_AREA)) {
            showDirOption(tile, tileItem, tileItemIdx)
            ImGui.separator()
        }

        if (isMovable) {
            imGuiMenuItem("Move To Top##move_to_top_$tileItemIdx") {
                viewController.doMoveToTop(tile, tileItem, tileItemIdx)
            }

            imGuiMenuItem("Move To Bottom##move_to_bottom_$tileItemIdx") {
                viewController.doMoveToBottom(tile, tileItem, tileItemIdx)
            }

            ImGui.separator()
        }

        imGuiMenuItem("Make Active Object##make_active_object_$tileItemIdx", shortcut = "Shift+LMB") {
            viewController.doMakeActiveObject(tileItem)
        }

        imGuiMenuItem("Edit...##edit_variables_$tileItemIdx", shortcut = "Shift+RMB") {
            viewController.doEdit(tile, tileItemIdx)
        }

        imGuiMenuItem("Delete##delete_object_$tileItemIdx", shortcut = "Ctrl+Shift+LMB") {
            viewController.doDeleteObject(tile, tileItemIdx)
        }

        imGuiMenuItem(
            "Replace With Selected Object##replace_with_selected_object_$tileItemIdx",
            shortcut = "Ctrl+Shift+RMB",
            enabled = viewController.isSameTypeAsSelectedObject(tileItem)
        ) {
            viewController.doReplaceWithSelectedTileItem(tile, tileItemIdx)
        }

        imGuiMenuItem("Reset to Default##reset_to_default_$tileItemIdx") {
            viewController.doResetToDefault(tile, tileItem, tileItemIdx)
        }
    }

    private fun showNudgeOption(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        val (initialValue, pixelNudge) = viewController.getNudgeValueToShow(isXAxis, tileItem, tileItemIdx)
        val axisName = if (isXAxis) "X" else "Y"

        ImGui.setNextItemWidth(50f)

        if (ImGui.dragInt("Nudge $axisName-axis###nudge_${axisName}_option_$tileItemIdx", pixelNudge, .25f)) {
            viewController.doNudge(isXAxis, tile, tileItem, tileItemIdx, pixelNudge)
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            viewController.applyNudgeChanges(isXAxis, tile, tileItem, tileItemIdx, pixelNudge, initialValue)
        }
    }

    private fun showDirOption(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        val maxDirs = GlobalDmiHolder.getIconState(tileItem.icon, tileItem.iconState)?.dirs ?: 1

        if (maxDirs == 1) {
            ImGui.textDisabled("No Dirs to Choose")
            return
        }

        val (initialValue, relativeDir) = viewController.getDirValueToShow(tileItem, tileItemIdx)
        val realDir = relToDir(relativeDir[0])

        ImGui.setNextItemWidth(100f)

        if (ImGui.sliderInt("Dir [${dirToStr(realDir)}]###dir_option_$tileItemIdx", relativeDir, 1, maxDirs, "$realDir")) {
            viewController.doDir(tile, tileItem, tileItemIdx, relativeDir)
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            viewController.applyDirChanges(tile, tileItem, tileItemIdx, initialValue, relativeDir)
        }
    }
}
