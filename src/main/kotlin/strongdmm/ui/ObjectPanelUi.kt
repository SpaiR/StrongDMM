package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImInt
import imgui.enums.ImGuiMouseButton
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerInstanceController
import strongdmm.event.type.controller.TriggerTileItemController
import strongdmm.event.type.ui.TriggerEditVarsDialogUi
import strongdmm.event.type.ui.TriggerInstanceLocatorPanelUi
import strongdmm.event.type.ui.TriggerObjectPanelUi
import strongdmm.util.imgui.*
import strongdmm.window.AppWindow

class ObjectPanelUi : EventConsumer, EventSender {
    companion object {
        private const val ICON_SIZE: Float = 32f
    }

    private val showVarsPreview: ImBool = ImBool(true)
    private val columnsCount: ImInt = ImInt(1)

    private var scrolledToItem: Boolean = false
    private var tileItemType: String = ""
    private var tileItems: List<TileItem>? = null
    private var selectedObjIdx: Int = 0

    init {
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(TriggerObjectPanelUi.Update::class.java, ::handleUpdate)
    }

    fun process() {
        setNextWindowPos(10f, AppWindow.windowHeight / 1.7f, AppWindow.defaultWindowCond)
        setNextWindowSize(330f, AppWindow.windowHeight - AppWindow.windowHeight / 1.7f - 15, AppWindow.defaultWindowCond)

        val title = if (tileItems?.size ?: 0 > 0) "(${tileItems!!.size}) $tileItemType###object_panel" else "Object Panel###object_panel"

        window(title) {
            popupContextItem("object_panel_config", ImGuiMouseButton.Right) {
                if (tileItemType.isNotEmpty()) {
                    button("Copy Type To Clipboard") {
                        setClipboardText(tileItemType)
                    }
                }
                checkbox("Show Variables Preview", showVarsPreview)
                setNextItemWidth(75f)
                if (inputInt("Columns count", columnsCount)) {
                    if (columnsCount.get() <= 0) {
                        columnsCount.set(1)
                    } else if (columnsCount.get() > 64) { // 64 - maximum number of columns in ImGui
                        columnsCount.set(64)
                    }
                }
            }

            columns(columnsCount.get())

            tileItems?.forEachIndexed { index, tileItem ->
                val isSelected = index == selectedObjIdx
                selectable("##tile_item_$index", selected = isSelected, sizeX = getColumnWidth() - 1f, sizeY = ICON_SIZE) {
                    sendEvent(TriggerTileItemController.ChangeActiveTileItem(tileItem))
                    scrolledToItem = true // do not scroll panel in the next cycle
                }
                if (isSelected && !scrolledToItem) {
                    setScrollHereY()
                    scrolledToItem = true
                }
                popupContextItem("object_options_$index", ImGuiMouseButton.Right) {
                    menuItem("Find Instance on Map") {
                        sendEvent(TriggerInstanceLocatorPanelUi.SearchById(tileItem.id))
                    }
                    menuItem("Fine All Objects on Map") {
                        sendEvent(TriggerInstanceLocatorPanelUi.SearchByType(tileItem.type))
                    }
                    separator()
                    menuItem("New Instance...") {
                        sendEvent(TriggerEditVarsDialogUi.OpenWithTileItem(tileItem))
                    }
                    menuItem("Generate Instances from Icon-states") {
                        sendEvent(TriggerInstanceController.GenerateInstancesFromIconStates(tileItem) {
                            handleUpdate()
                        })
                    }
                    menuItem("Generate Instances from Directions") {
                        sendEvent(TriggerInstanceController.GenerateInstancesFromDirections(tileItem) {
                            handleUpdate()
                        })
                    }
                }

                sameLine()
                withIndent(36f) {
                    text(tileItem.name)
                }

                sameLine()
                withIndent(1f) {
                    GlobalDmiHolder.getIconSpriteOrPlaceholder(tileItem.icon, tileItem.iconState, tileItem.dir).run {
                        image(textureId, ICON_SIZE, ICON_SIZE, u1, v1, u2, v2, tileItem.colorR, tileItem.colorG, tileItem.colorB, 1f)
                    }
                }

                nextColumn()
            }
        }

        if (showVarsPreview.get()) {
            setNextWindowPos(345f, AppWindow.windowHeight - 210f, AppWindow.defaultWindowCond)
            setNextWindowSize(300f, 195f, AppWindow.defaultWindowCond)

            window("Variables Preview", showVarsPreview) {
                tileItems?.let { objs ->
                    if (selectedObjIdx != -1) {
                        val tileItem = objs[selectedObjIdx]

                        if (tileItem.customVars == null) {
                            text("Empty (instance with initial vars)")
                        } else {
                            columns(2)
                            tileItem.customVars.forEach { (name, value) ->
                                textColored(0f, 1f, 0f, 1f, name)
                                nextColumn()
                                text(value)
                                nextColumn()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getTileItemsByTypeSorted(type: String): List<TileItem> {
        val tileItems = GlobalTileItemHolder.getTileItemsByType(type).sortedBy { it.name }.sortedBy { it.iconState }.toMutableList()
        val initialItem = GlobalTileItemHolder.getOrCreate(type)
        tileItems.remove(initialItem)
        tileItems.add(0, initialItem)
        return tileItems
    }

    private fun handleEnvironmentReset() {
        tileItemType = ""
        tileItems = null
        selectedObjIdx = 0
    }

    private fun handleActiveTileItemChanged(event: Event<TileItem?, Unit>) {
        if (event.body != null) {
            scrolledToItem = false
            tileItemType = event.body.type
            tileItems = getTileItemsByTypeSorted(event.body.type)
            selectedObjIdx = tileItems!!.withIndex().find { it.value.customVars == event.body.customVars }?.index ?: 0
        }
    }

    private fun handleSelectedMapChanged() {
        if (tileItemType.isNotEmpty()) {
            tileItems = getTileItemsByTypeSorted(tileItemType)
        }
    }

    private fun handleUpdate() {
        if (tileItemType.isNotEmpty()) {
            tileItems = getTileItemsByTypeSorted(tileItemType)
        }
    }
}
