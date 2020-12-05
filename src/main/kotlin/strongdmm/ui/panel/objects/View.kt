package strongdmm.ui.panel.objects

import imgui.ImGui
import imgui.flag.ImGuiMouseButton
import strongdmm.byond.dmm.TileItem
import strongdmm.ui.LayoutManager
import strongdmm.util.imgui.*
import strongdmm.application.window.Window

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

        private const val CONFIG_POPUP_TITLE = "object_panel_config"
    }

    lateinit var viewController: ViewController

    fun process() {
        ImGui.setNextWindowPos(LayoutManager.Bottom.Left.posX, LayoutManager.Bottom.Left.posY, Window.windowCond)
        ImGui.setNextWindowSize(LayoutManager.Bottom.Left.width, LayoutManager.Bottom.Left.height, Window.windowCond)

        imGuiBegin(viewController.getTitle()) {
            showConfigContextMenu()

            ImGui.columns(state.columnsCount.get())

            state.tileItems?.forEach { tileItem ->
                val isSelected = tileItem.id == state.selectedTileItemId

                imGuiSelectable("##tile_item_${tileItem.id}", selected = isSelected, sizeX = ImGui.getColumnWidth() - iconIndent, sizeY = iconSize) {
                    viewController.doSelectItem(tileItem)
                }

                if (isSelected && !state.scrolledToItem) {
                    ImGui.setScrollHereY()
                    state.scrolledToItem = true
                }

                showItemContextMenu(tileItem)

                ImGui.sameLine()
                imGuiWithIndent(textIndent) {
                    ImGui.text(tileItem.name)
                }

                ImGui.sameLine()
                imGuiWithIndent(iconIndent) {
                    viewController.getIconSprite(tileItem).run {
                        ImGui.image(textureId, iconSize, iconSize, u1, v1, u2, v2, tileItem.colorR, tileItem.colorG, tileItem.colorB, 1f)
                    }
                }

                ImGui.nextColumn()
            }
        }
    }

    private fun showConfigContextMenu() {
        ImGuiExt.windowButton(ImGuiIconFA.COG) {
            ImGui.openPopup(CONFIG_POPUP_TITLE)
        }

        imGuiPopup(CONFIG_POPUP_TITLE) {
            if (state.selectedTileItemType.isNotEmpty()) {
                imGuiButton("Copy Type To Clipboard") {
                    ImGui.setClipboardText(state.selectedTileItemType)
                }
            }

            ImGui.setNextItemWidth(columnsCountInputWidth)

            if (ImGui.inputInt("Columns", state.columnsCount)) {
                if (state.columnsCount.get() <= 0) {
                    state.columnsCount.set(1)
                } else if (state.columnsCount.get() > 16) {
                    state.columnsCount.set(16)
                }
            }
        }
    }

    private fun showItemContextMenu(tileItem: TileItem) {
        imGuiPopupContextItem("object_options_${tileItem.id}", ImGuiMouseButton.Right) {
            imGuiMenuItem("Find Instance on Map") {
                viewController.doFindInstanceOnMap(tileItem)
            }

            imGuiMenuItem("Find Object on Map") {
                viewController.doFindObjectOnMap(tileItem)
            }

            ImGui.separator()

            imGuiMenuItem("New Instance...") {
                viewController.doNewInstance(tileItem)
            }

            imGuiMenuItem("Edit Instance...") {
                viewController.doEditInstance(tileItem)
            }

            imGuiMenuItem("Delete Instance...") {
                viewController.doDeleteInstance(tileItem)
            }

            ImGui.separator()

            imGuiMenuItem("Generate Instances from Icon-states") {
                viewController.doGenerateInstancesFromIconStates(tileItem)
            }

            imGuiMenuItem("Generate Instances from Directions") {
                viewController.doGenerateInstancesFromDirections(tileItem)
            }
        }
    }
}
