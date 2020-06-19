package strongdmm.ui.panel.objects

import imgui.ImGui.*
import imgui.enums.ImGuiMouseButton
import strongdmm.byond.dmm.TileItem
import strongdmm.util.imgui.*
import strongdmm.window.Window
import strongdmm.window.WindowUtil

class View(
    private val state: State
) {
    companion object {
        private const val ICON_SIZE: Float = 32f

        private const val POS_X: Float = 10f
        private const val POS_Y_PERCENT: Int = 60

        private const val WIDTH: Float = 330f
    }

    lateinit var viewController: ViewController

    fun process() {
        val height = WindowUtil.getHeightPercent(POS_Y_PERCENT)

        ImGuiUtil.setNextPosAndSize(POS_X, height, WIDTH, Window.windowHeight - height - 15)

        window(viewController.getTitle()) {
            showConfigContextMenu()

            columns(state.columnsCount.get())

            state.tileItems?.forEach { tileItem ->
                val isSelected = tileItem.id == state.selectedTileItemId

                selectable("##tile_item_${tileItem.id}", selected = isSelected, sizeX = getColumnWidth() - 1f, sizeY = ICON_SIZE) {
                    viewController.doSelectItem(tileItem)
                }

                if (isSelected && !state.scrolledToItem) {
                    setScrollHereY()
                    state.scrolledToItem = true
                }

                showItemContextMenu(tileItem)

                sameLine()
                withIndent(36f) {
                    text(tileItem.name)
                }

                sameLine()
                withIndent(1f) {
                    viewController.getIconSprite(tileItem).run {
                        image(textureId, ICON_SIZE, ICON_SIZE, u1, v1, u2, v2, tileItem.colorR, tileItem.colorG, tileItem.colorB, 1f)
                    }
                }

                nextColumn()
            }
        }
    }

    private fun showConfigContextMenu() {
        popupContextItem("object_panel_config", ImGuiMouseButton.Right) {
            if (state.selectedTileItemType.isNotEmpty()) {
                button("Copy Type To Clipboard") {
                    setClipboardText(state.selectedTileItemType)
                }
            }

            setNextItemWidth(75f)

            if (inputInt("Columns count", state.columnsCount)) {
                if (state.columnsCount.get() <= 0) {
                    state.columnsCount.set(1)
                } else if (state.columnsCount.get() > 64) { // 64 - maximum number of columns in ImGui
                    state.columnsCount.set(64)
                }
            }
        }
    }

    private fun showItemContextMenu(tileItem: TileItem) {
        popupContextItem("object_options_${tileItem.id}", ImGuiMouseButton.Right) {
            menuItem("Find Instance on Map") {
                viewController.doFindInstanceOnMap(tileItem)
            }

            menuItem("Fine Object on Map") {
                viewController.doFindObjectOnMap(tileItem)
            }

            separator()

            menuItem("New Instance...") {
                viewController.doNewInstance(tileItem)
            }

            menuItem("Generate Instances from Icon-states") {
                viewController.doGenerateInstancesFromIconStates(tileItem)
            }

            menuItem("Generate Instances from Directions") {
                viewController.doGenerateInstancesFromDirections(tileItem)
            }
        }
    }
}
