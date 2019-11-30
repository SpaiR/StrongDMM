package strongdmm.ui.vars

import glm_.vec2.Vec2
import imgui.*
import imgui.ImGui.alignTextToFramePadding
import imgui.ImGui.checkbox
import imgui.ImGui.columns
import imgui.ImGui.endColumns
import imgui.ImGui.inputText
import imgui.ImGui.nextColumn
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextItemWidth
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.dsl.button
import imgui.dsl.child
import imgui.dsl.tooltip
import imgui.dsl.window
import imgui.dsl.withStyleColor
import imgui.internal.strlen
import strongdmm.byond.*
import strongdmm.byond.dme.DmeItem
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.byond.dmm.TileItemIdx
import strongdmm.controller.action.ReplaceTileAction
import strongdmm.controller.canvas.CanvasBlockStatus
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.GREEN32
import strongdmm.util.LMB

class EditVarsDialogUi : EventSender, EventConsumer {
    companion object {
        private const val DIALOG_WIDTH: Int = 400
        private const val DIALOG_HEIGHT: Int = 450

        private const val FILTER_BUFFER: Int = 1024

        private var WINDOW_ID: Long = 0 // To ensure every window will be unique.

        private val HIDDEN_VARS: Set<String> = setOf(
            VAR_TYPE, VAR_PARENT_TYPE, VAR_VARS, VAR_X, VAR_Y, VAR_Z, VAR_CONTENTS, VAR_FILTERS, VAR_LOC, VAR_MAPTEXT,
            VAR_MAPTEXT_WIDTH, VAR_MAPTEXT_HEIGHT, VAR_MAPTEXT_X, VAR_MAPTEXT_Y, VAR_OVERLAYS, VAR_UNDERLAYS, VAR_VERBS,
            VAR_APPEARANCE, VAR_VIS_CONTENTS, VAR_VIS_LOCS
        )
    }

    private var currentTile: Tile? = null
    private var currentTileItemIndex: TileItemIdx = TileItemIdx(0) // This index is an item index inside of Tile list of objects
    private var currentEditVar: Var? = null

    private var varsFilterRaw: CharArray = CharArray(FILTER_BUFFER) // Buffer for variable filter
    private var varsFilter: String = "" // The actual string representation of the the filter

    private val isShowInstanceVars: MutableProperty0<Boolean> = MutableProperty0(false)
    private val variables: MutableList<Var> = mutableListOf()

    init {
        consumeEvent(Event.EditVarsDialogUi.Open::class.java, ::handleOpen)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
    }

    fun process(windowWidth: Int, windowHeight: Int) {
        if (currentTile == null) {
            return
        }

        getTileItem()?.let { tileItem ->
            setNextWindowPos(Vec2((windowWidth - DIALOG_WIDTH) / 2, (windowHeight - DIALOG_HEIGHT) / 2), Cond.Once)
            setNextWindowSize(Vec2(DIALOG_WIDTH, DIALOG_HEIGHT), Cond.Once)

            window("Edit Variables: ${tileItem.type}##$WINDOW_ID") {
                drawControls()

                separator()

                child("vars_table") {
                    drawVariables()
                }
            }
        }
    }

    private fun drawControls() {
        checkbox("##isShowInstanceVars", isShowInstanceVars)
        if (ImGui.isItemHovered()) {
            tooltip { text("Show only instance variables") }
        }

        sameLine()
        if (inputText("##varsFilter", varsFilterRaw)) {
            varsFilter = String(varsFilterRaw, 0, varsFilterRaw.strlen)
        }

        sameLine()
        button("OK") {
            applyModifiedVars()
            dispose()
        }

        sameLine()
        button("Cancel", block = ::dispose)
    }

    private fun drawVariables() {
        columns(2, border = true)

        for (variable in variables) {
            if (isShowInstanceVars.get() && !variable.isModified) {
                continue
            }

            if (varsFilter.isNotEmpty() && !variable.name.contains(varsFilter)) {
                continue
            }

            alignTextToFramePadding()
            if (variable.isModified) {
                withStyleColor(Col.Text, GREEN32) {
                    text(variable.name)
                }
            } else {
                text(variable.name)
            }

            nextColumn()
            setNextItemWidth(ImGui.getColumnWidth(-1))

            if (variable === currentEditVar) {
                if (inputText("##${variable.name}", variable.buffer!!, InputTextFlag.EnterReturnsTrue.i)) {
                    currentEditVar?.stopEdit()
                    currentEditVar = null
                }
            } else {
                alignTextToFramePadding()
                text(variable.displayValue)

                if (ImGui.isItemHovered()) {
                    ImGui.mouseCursor = MouseCursor.Hand
                }

                if (ImGui.isItemClicked(LMB)) {
                    currentEditVar?.stopEdit()
                    currentEditVar = variable
                    variable.startEdit()
                }
            }

            nextColumn()
            separator()
        }

        endColumns()
    }

    private fun collectVarsToDisplay() {
        currentEditVar = null
        variables.clear()

        fun collectVars(dmeItem: DmeItem) {
            dmeItem.vars.filterKeys { it !in HIDDEN_VARS }.forEach { (name, value) ->
                if (variables.none { it.name == name }) {
                    variables.add(Var(name, value ?: "null", value ?: "null"))
                }
            }

            dmeItem.getParent()?.let { collectVars(it) }
        }

        getTileItem()?.let { tileItem ->
            tileItem.customVars?.filterKeys { it !in HIDDEN_VARS }?.forEach { name, value ->
                variables.add(Var(name, value, tileItem.dmeItem.getVar(name) ?: "null"))
            }

            collectVars(tileItem.dmeItem)
        }

        variables.sortBy { it.name }
    }

    private fun getTileItem(): TileItem? {
        return when (currentTileItemIndex) {
            TileItemIdx.AREA -> currentTile?.area
            TileItemIdx.TURF -> currentTile?.turf
            else -> currentTile?.tileItems?.get(currentTileItemIndex.value)
        }
    }

    private fun applyModifiedVars() {
        currentEditVar?.stopEdit()

        if (variables.none { it.isChanged }) {
            return
        }

        val newItemVars = mutableMapOf<String, String>()

        variables.filter { it.isChanged || it.isModified }.forEach {
            newItemVars[it.name] = it.value
        }

        currentTile?.let { tile ->
            sendEvent(Event.ActionController.AddAction(
                ReplaceTileAction(tile) {
                    tile.modifyItemVars(currentTileItemIndex, if (newItemVars.isEmpty()) null else newItemVars)
                }
            ))

            sendEvent(Event.Global.RefreshFrame())
        }
    }

    private fun dispose() {
        currentTile = null
        currentEditVar = null
        varsFilterRaw = CharArray(FILTER_BUFFER)
        varsFilter = ""
        variables.clear()
        sendEvent(Event.CanvasController.Block(CanvasBlockStatus(false)))
    }

    private fun handleOpen(event: Event<Pair<Tile, TileItemIdx>, Unit>) {
        sendEvent(Event.CanvasController.Block(CanvasBlockStatus(true)))
        WINDOW_ID++
        currentTile = event.body.first
        currentTileItemIndex = event.body.second
        collectVarsToDisplay()
    }

    private fun handleResetEnvironment() {
        dispose()
    }

    private fun handleCloseMap() {
        dispose()
    }
}
