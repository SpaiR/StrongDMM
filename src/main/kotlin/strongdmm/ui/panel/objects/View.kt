package strongdmm.ui.panel.objects

import imgui.ImGui.*
import imgui.flag.ImGuiMouseButton
import strongdmm.byond.dmm.TileItem
import strongdmm.util.imgui.*
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val iconSize: Float
            get() = 32f * Window.pointSize

        private val textIndent: Float
            get() = 36f * Window.pointSize
        private val iconIndent: Float
            get() = 1f * Window.pointSize

        private val columnsCountInputWidth: Float
            get() = 75f * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        setNextWindowPos(ObjectsPanelUi.posX, ObjectsPanelUi.posY, Window.windowCond)
        setNextWindowSize(ObjectsPanelUi.width, ObjectsPanelUi.height, Window.windowCond)

        window(viewController.getTitle()) {
            showConfigContextMenu()

            columns(state.columnsCount.get())

            state.tileItems?.forEach { tileItem ->
                val isSelected = tileItem.id == state.selectedTileItemId

                selectable("##tile_item_${tileItem.id}", selected = isSelected, sizeX = getColumnWidth() - iconIndent, sizeY = iconSize) {
                    viewController.doSelectItem(tileItem)
                }

                if (isSelected && !state.scrolledToItem) {
                    setScrollHereY()
                    state.scrolledToItem = true
                }

                showItemContextMenu(tileItem)

                sameLine()
                withIndent(textIndent) {
                    text(tileItem.name)
                }

                sameLine()
                withIndent(iconIndent) {
                    viewController.getIconSprite(tileItem).run {
                        image(textureId, iconSize, iconSize, u1, v1, u2, v2, tileItem.colorR, tileItem.colorG, tileItem.colorB, 1f)
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

            setNextItemWidth(columnsCountInputWidth)

            if (inputInt("Columns count", state.columnsCount)) {
                if (state.columnsCount.get() <= 0) {
                    state.columnsCount.set(1)
                } else if (state.columnsCount.get() > 16) {
                    state.columnsCount.set(16)
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
