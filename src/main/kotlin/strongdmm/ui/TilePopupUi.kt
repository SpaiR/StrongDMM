package strongdmm.ui

import glm_.vec2.Vec2
import imgui.ImGui.image
import imgui.ImGui.openPopup
import imgui.ImGui.sameLine
import imgui.ImGui.separator
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
        currentTile?.let { tile ->
            popup("tile_popup", WindowFlag.NoMove.i) {
                showTileItems(tile)
            }
        }
    }

    private fun showTileItems(tile: Tile) {
        tile.area?.let { area -> showTileItemRow(tile, area, Tile.AREA_INDEX) }
        tile.mobs.forEach { mob -> showTileItemRow(tile, mob.value, mob.index) }
        tile.objs.forEach { obj -> showTileItemRow(tile, obj.value, obj.index) }
        tile.turf?.let { turf -> showTileItemRow(tile, turf, Tile.TURF_INDEX) }
    }

    private fun showTileItemRow(tile: Tile, tileItem: TileItem, index: Int) {
        val sprite = GlobalDmiHolder.getSprite(tileItem.icon, tileItem.iconState, tileItem.dir)
        val name = tileItem.getVarText(VAR_NAME)!!

        image(sprite.textureId, ICON_SIZE, Vec2(sprite.u1, sprite.v1), Vec2(sprite.u2, sprite.v2))
        sameLine()

        menu("$name##$index") {
            showTileItemOptions(tile, tileItem, index)
        }

        sameLine()
        text("[${tileItem.type}]  ") // Two spaces in the end to make text not to overlap over the menu arrow.
    }

    private fun showTileItemOptions(tile: Tile, tileItem: TileItem, index: Int) {
        if (index != Tile.AREA_INDEX && index != Tile.TURF_INDEX) {
            menuItem("Move To Top##$index") {
                tile.moveToTop(tileItem.type.startsWith(TYPE_MOB), index)
                sendEvent(Event.Global.RefreshFrame())
            }
            menuItem("Move To Bottom##$index") {
                tile.moveToBottom(tileItem.type.startsWith(TYPE_MOB), index)
                sendEvent(Event.Global.RefreshFrame())
            }

            separator()
        }

        menuItem("Edit Variables...##$index") {
            sendEvent(Event.EditVarsDialogUi.Open(Pair(tile, index)))
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
