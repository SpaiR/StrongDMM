package strongdmm.ui.tile_popup

import imgui.ImGui.*
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.dirToStr
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.byond.relToDir
import strongdmm.util.imgui.menu
import strongdmm.util.imgui.menuItem
import strongdmm.util.imgui.popup

class View(
    private val state: State
) {
    companion object {
        private const val POPUP_ID: String = "tile_popup"
        private const val ICON_SIZE: Float = 16f
    }

    lateinit var viewController: ViewController

    fun process() {
        state.currentTile?.let { tile ->
            if (state.isDoOpen) {
                openPopup(POPUP_ID)
                state.isDoOpen = false
            } else if (!isPopupOpen(POPUP_ID)) { // if it closed - it closed
                viewController.dispose()
                return
            }

            popup(POPUP_ID) {
                menuItem("Undo", shortcut = "Ctrl+Z", enabled = state.isUndoEnabled, block = viewController::doUndo)
                menuItem("Redo", shortcut = "Ctrl+Shift+Z", enabled = state.isRedoEnabled, block = viewController::doRedo)
                separator()
                menuItem("Cut", shortcut = "Ctrl+X", block = viewController::doCut)
                menuItem("Copy", shortcut = "Ctrl+C", block = viewController::doCopy)
                menuItem("Paste", shortcut = "Ctrl+V", block = viewController::doPaste)
                menuItem("Delete", shortcut = "Delete", block = viewController::doDelete)
                menuItem("Deselect All", shortcut = "Ctrl+D", block = viewController::doDeselectAll)
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

        image(sprite.textureId, ICON_SIZE, ICON_SIZE, sprite.u1, sprite.v1, sprite.u2, sprite.v2, tileItem.colorR, tileItem.colorG, tileItem.colorB, 1f)
        sameLine()
        menu("${tileItem.name}##tile_item_row_$tileItemIdx") {
            showTileItemOptions(tile, tileItem, tileItemIdx)
        }
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
                viewController.doMoveToTop(tile, tileItem, tileItemIdx)
            }

            menuItem("Move To Bottom##move_to_bottom_$tileItemIdx") {
                viewController.doMoveToBottom(tile, tileItem, tileItemIdx)
            }

            separator()
        }

        menuItem("Make Active Object##make_active_object_$tileItemIdx", shortcut = "Shift+LMB") {
            viewController.doMakeActiveObject(tileItem)
        }

        menuItem("Edit...##edit_variables_$tileItemIdx", shortcut = "Shift+RMB") {
            viewController.doEdit(tile, tileItemIdx)
        }

        menuItem("Delete##delete_object_$tileItemIdx", shortcut = "Ctrl+Shift+LMB") {
            viewController.doDeleteObject(tile, tileItemIdx)
        }

        menuItem(
            "Replace With Selected Object##replace_with_selected_object_$tileItemIdx",
            shortcut = "Ctrl+Shift+RMB",
            enabled = viewController.isSameTypeAsSelectedObject(tileItem)
        ) {
            viewController.doReplaceWithSelectedTileItem(tile, tileItemIdx)
        }

        menuItem("Reset to Default##reset_to_default_$tileItemIdx") {
            viewController.doResetToDefault(tile, tileItem, tileItemIdx)
        }
    }

    private fun showNudgeOption(isXAxis: Boolean, tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        val (initialValue, pixelNudge) = viewController.getNudgeValueToShow(isXAxis, tileItem, tileItemIdx)
        val axisName = if (isXAxis) "X" else "Y"

        setNextItemWidth(50f)

        if (dragInt("Nudge $axisName-axis###nudge_${axisName}_option_$tileItemIdx", pixelNudge, .25f)) {
            viewController.doNudge(isXAxis, tile, tileItem, tileItemIdx, pixelNudge)
        }

        if (isItemDeactivatedAfterEdit()) {
            viewController.applyNudgeChanges(isXAxis, tile, tileItem, tileItemIdx, pixelNudge, initialValue)
        }
    }

    private fun showDirOption(tile: Tile, tileItem: TileItem, tileItemIdx: Int) {
        val maxDirs = GlobalDmiHolder.getIconState(tileItem.icon, tileItem.iconState)?.dirs ?: 1

        if (maxDirs == 1) {
            textDisabled("No Dirs to Choose")
            return
        }

        val (initialValue, relativeDir) = viewController.getDirValueToShow(tileItem, tileItemIdx)
        val realDir = relToDir(relativeDir[0])

        setNextItemWidth(100f)

        if (sliderInt("Dir [${dirToStr(realDir)}]###dir_option_$tileItemIdx", relativeDir, 1, maxDirs, "$realDir")) {
            viewController.doDir(tile, tileItem, tileItemIdx, relativeDir)
        }

        if (isItemDeactivatedAfterEdit()) {
            viewController.applyDirChanges(tile, tileItem, tileItemIdx, initialValue, relativeDir)
        }
    }
}
