package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImInt
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiMouseButton
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.EventInstanceController
import strongdmm.event.type.controller.EventTileItemController
import strongdmm.event.type.ui.EventEditVarsDialogUi
import strongdmm.event.type.ui.EventInstanceLocatorPanelUi
import strongdmm.event.type.ui.EventObjectPanelUi
import strongdmm.util.imgui.*

class ObjectPanelUi : EventConsumer, EventSender {
    companion object {
        private const val ICON_SIZE: Float = 32f
    }

    private val showVarsPreview: ImBool = ImBool(false)
    private lateinit var showInstanceLocator: ImBool
    private val columnsCount: ImInt = ImInt(1)

    private var scrolledToItem: Boolean = false
    private var tileItemType: String = ""
    private var tileItems: List<TileItem>? = null
    private var selectedObjIdx: Int = 0

    init {
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(EventGlobal.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(EventGlobalProvider.InstanceLocatorOpen::class.java, ::handleProviderInstanceLocatorOpen)
        consumeEvent(EventObjectPanelUi.Update::class.java, ::handleUpdate)
    }

    fun process() {
        setNextWindowPos(10f, 535f, ImGuiCond.Once)
        setNextWindowSize(330f, 390f, ImGuiCond.Once)

        val title = if (tileItems?.size ?: 0 > 0) "(${tileItems!!.size}) $tileItemType###object_panel" else "Object Panel###object_panel"

        window(title) {
            popupContextItem("object_panel_config", ImGuiMouseButton.Right) {
                checkbox("Show Variables Preview", showVarsPreview)
                checkbox("Show Instance Locator", showInstanceLocator)
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
                    sendEvent(EventTileItemController.ChangeActive(tileItem))
                    scrolledToItem = true // do not scroll panel in the next cycle
                }
                if (isSelected && !scrolledToItem) {
                    setScrollHereY()
                    scrolledToItem = true
                }
                popupContextItem("object_options_$index", ImGuiMouseButton.Right) {
                    menuItem("Find Instance on Map") {
                        sendEvent(EventInstanceLocatorPanelUi.SearchById(tileItem.id))
                    }
                    menuItem("Fine All Objects on Map") {
                        sendEvent(EventInstanceLocatorPanelUi.SearchByType(tileItem.type))
                    }
                    separator()
                    menuItem("New Instance...") {
                        sendEvent(EventEditVarsDialogUi.OpenWithTileItem(tileItem))
                    }
                    menuItem("Generate Instances from Icon-states") {
                        sendEvent(EventInstanceController.GenerateFromIconStates(tileItem) {
                            handleUpdate()
                        })
                    }
                    menuItem("Generate Instances from Directions") {
                        sendEvent(EventInstanceController.GenerateFromDirections(tileItem) {
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
            setNextWindowPos(345f, 730f, ImGuiCond.Once)
            setNextWindowSize(300f, 195f, ImGuiCond.Once)

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
        return GlobalTileItemHolder.getTileItemsByType(type).sortedBy { it.iconState }.sortedBy { it.dir }.sortedBy { it.customVars?.size }
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

    private fun handleProviderInstanceLocatorOpen(event: Event<ImBool, Unit>) {
        showInstanceLocator = event.body
    }

    private fun handleUpdate() {
        if (tileItemType.isNotEmpty()) {
            tileItems = getTileItemsByTypeSorted(tileItemType)
        }
    }
}
