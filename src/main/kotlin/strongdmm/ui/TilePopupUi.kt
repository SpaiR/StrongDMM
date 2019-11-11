package strongdmm.ui

import glm_.vec2.Vec2
import imgui.ImGui.image
import imgui.ImGui.openPopup
import imgui.ImGui.sameLine
import imgui.ImGui.text
import imgui.SelectableFlag
import imgui.WindowFlag
import imgui.dsl.menu
import imgui.dsl.popup
import imgui.dsl.selectable
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.VAR_NAME
import strongdmm.byond.dmi.GlobalDmiHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender

class TilePopupUi : EventConsumer, EventSender {
    companion object {
        private const val AREA_INDEX: Int = -1
        private const val TURF_INDEX: Int = -2

        private val ICON_SIZE: Vec2 = Vec2(16, 16)
    }

    private var currentTile: Tile? = null

    init {
        consumeEvent(Event.TilePopupUi.Open::class.java, ::handleOpen)
        consumeEvent(Event.TilePopupUi.Close::class.java, ::handleClose)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
    }

    fun process() {
        if (currentTile != null) {
            popup("tile_popup", WindowFlag.NoMove.i) {
                showTileItems()
            }
        }
    }

    private fun showTileItems() {
        currentTile?.area?.let { showTileItemRow(it, AREA_INDEX) }
        currentTile?.mobs?.forEach { showTileItemRow(it.value, it.index) }
        currentTile?.objs?.forEach { showTileItemRow(it.value, it.index) }
        currentTile?.turf?.let { showTileItemRow(it, TURF_INDEX) }
    }

    private fun showTileItemRow(tileItem: TileItem, index: Int) {
        val sprite = GlobalDmiHolder.getSprite(tileItem.icon, tileItem.iconState, tileItem.dir)
        val name = tileItem.getVarText(VAR_NAME)!!

        image(sprite.textureId, ICON_SIZE, Vec2(sprite.u1, sprite.v1), Vec2(sprite.u2, sprite.v2))
        sameLine()

        menu("$name##$index") {
            showTileItemOptions(tileItem, index)
        }

        sameLine()
        text("[${tileItem.type}]  ") // Two spaces in the end to make text not to overlap over the menu arrow.
    }

    private fun showTileItemOptions(tileItem: TileItem, index: Int) {
        if (index != AREA_INDEX && index != TURF_INDEX) {
            menuItem("Move To Top##$index") {
                currentTile?.moveToTop(tileItem.type.startsWith(TYPE_MOB), index)
                sendEvent(Event.Global.RefreshFrame())
            }
            menuItem("Move To Bottom##$index") {
                currentTile?.moveToBottom(tileItem.type.startsWith(TYPE_MOB), index)
                sendEvent(Event.Global.RefreshFrame())
            }
        }
    }

    // Default imgui.dsl.menuItem throws the exception after selection.
    private inline fun menuItem(label: String, block: () -> Unit) {
        selectable(label, flags = SelectableFlag.DontClosePopups.i) {
            block()
            currentTile = null
        }
    }

    private fun handleOpen(event: Event<Tile, Unit>) {
        currentTile = event.body
        openPopup("tile_popup")
    }

    private fun handleClose() {
        currentTile = null
    }

    private fun handleResetEnvironment() {
        currentTile = null
    }

    private fun handleCloseMap() {
        currentTile = null
    }
}
