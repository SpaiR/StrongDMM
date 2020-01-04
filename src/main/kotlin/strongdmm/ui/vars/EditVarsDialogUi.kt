package strongdmm.ui.vars

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiMouseCursor
import imgui.enums.ImGuiStyleVar
import org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER
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
import strongdmm.util.imgui.*

class EditVarsDialogUi : EventSender, EventConsumer {
    companion object {
        private const val DIALOG_WIDTH: Float = 400f
        private const val DIALOG_HEIGHT: Float = 450f
        private const val FILTER_BUFFER: Int = 10

        private var WINDOW_ID: Long = 0 // To ensure every window will be unique.

        private val HIDDEN_VARS: Set<String> = setOf(
            VAR_TYPE, VAR_PARENT_TYPE, VAR_VARS, VAR_X, VAR_Y, VAR_Z, VAR_CONTENTS, VAR_FILTERS, VAR_LOC, VAR_MAPTEXT,
            VAR_MAPTEXT_WIDTH, VAR_MAPTEXT_HEIGHT, VAR_MAPTEXT_X, VAR_MAPTEXT_Y, VAR_OVERLAYS, VAR_UNDERLAYS, VAR_VERBS,
            VAR_APPEARANCE, VAR_VIS_CONTENTS, VAR_VIS_LOCS
        )
    }

    private var currentTile: Tile? = null
    private var initialTileItemsId: IntArray? = null  // Used to restore tile state if we didn't save our modified vars
    private var currentTileItemIndex: TileItemIdx = TileItemIdx(0) // This index is an item index inside of Tile objects list
    private var currentEditVar: Var? = null

    // Variables filter buffer, resizable string
    private var varsFilter: ImString = ImString(FILTER_BUFFER).apply { inputData.isResizable = true }

    private val isShowModifiedVars: ImBool = ImBool(false)
    private val variables: MutableList<Var> = mutableListOf()
    private var variableInputFocused: Boolean = false // On first var select we will focus its input. Var is to check if we've already did it

    init {
        consumeEvent(Event.EditVarsDialogUi.Open::class.java, ::handleOpen)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
    }

    fun process(windowWidth: Int, windowHeight: Int) {
        if (currentTile == null) {
            return
        }

        getTileItem()?.let { tileItem ->
            setNextWindowPos((windowWidth - DIALOG_WIDTH) / 2f, (windowHeight - DIALOG_HEIGHT) / 2f, ImGuiCond.Once)
            setNextWindowSize(DIALOG_WIDTH, DIALOG_HEIGHT, ImGuiCond.Once)

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
        checkbox("##is_show_modified_vars", isShowModifiedVars).itemHovered {
            setTooltip("Show modified variables")
        }
        sameLine()
        inputText("##vars_filter", varsFilter)
        sameLine()
        button("OK", block = ::saveChangesAndDispose)
        sameLine()
        button("Cancel", block = ::discardChangesAndDispose)
    }

    private fun drawVariables() {
        var rowCount = 0

        columns(2, "var_columns", true)

        for (variable in variables) {
            if (isShowModifiedVars.get() && !(variable.isModified || variable.isChanged)) {
                continue
            }

            if (varsFilter.get().isNotEmpty() && !variable.name.contains(varsFilter.get())) {
                continue
            }

            alignTextToFramePadding()
            if (variable.isModified || variable.isChanged) {
                pushStyleColor(ImGuiCol.Text, GREEN32)
                text(variable.name)
                popStyleColor()
            } else {
                text(variable.name)
            }

            nextColumn()

            if (variable === currentEditVar) {
                setNextItemWidth(getColumnWidth(-1))
                inputText("##${variable.name}", variable.buffer!!)

                if (isKeyPressed(GLFW_KEY_ENTER) || isKeyPressed(GLFW_KEY_KP_ENTER) || isKeyPressed(GLFW_KEY_ESCAPE)) {
                    currentEditVar?.stopEdit()
                    currentEditVar = null
                    applyTmpTileChanges()
                }

                if (!variableInputFocused) {
                    setKeyboardFocusHere(-1)
                    variableInputFocused = true
                }
            } else {
                pushStyleColor(ImGuiCol.Button, 0)
                pushStyleColor(ImGuiCol.ButtonHovered, .25f, .58f, .98f, .5f)
                pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0f, 0f)

                button("${variable.value}##${rowCount++}", getColumnWidth(-1)) {
                    variableInputFocused = false

                    if (currentEditVar != null) {
                        currentEditVar!!.stopEdit()
                        applyTmpTileChanges()
                    }

                    currentEditVar = variable
                    variable.startEdit()
                }.itemHovered {
                    setMouseCursor(ImGuiMouseCursor.Hand)
                }

                popStyleVar()
                popStyleColor(2)
            }

            nextColumn()
            separator()
        }
    }

    private fun collectVarsToDisplay() {
        currentEditVar = null
        variables.clear()

        fun collectVars(dmeItem: DmeItem) {
            dmeItem.vars.filterKeys { variableName -> variableName !in HIDDEN_VARS }.forEach { (name, value) ->
                if (variables.none { variable -> variable.name == name }) {
                    variables.add(Var(name, value ?: "null", value ?: "null"))
                }
            }

            dmeItem.getParent()?.let { collectVars(it) }
        }

        getTileItem()?.let { tileItem ->
            tileItem.customVars?.filterKeys { variableName -> variableName !in HIDDEN_VARS }?.forEach { name, value ->
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

    private fun getNewItemVars(): Map<String, String>? {
        if (variables.none { it.isChanged }) {
            return null
        }

        val newItemVars = mutableMapOf<String, String>()

        variables.filter { it.isChanged || it.isModified }.forEach {
            newItemVars[it.name] = it.value
        }

        return newItemVars
    }

    private fun applyTmpTileChanges() {
        getNewItemVars()?.let { newItemVars ->
            currentTile?.modifyItemVars(currentTileItemIndex, if (newItemVars.isEmpty()) null else newItemVars)
            sendEvent(Event.Global.RefreshFrame())
        }
    }

    private fun discardTmpTileChanges() {
        if (currentTile != null && initialTileItemsId != null) {
            currentTile!!.replaceTileItemsId(initialTileItemsId!!)
            sendEvent(Event.Global.RefreshFrame())
        }
    }

    private fun saveChangesAndDispose() {
        currentEditVar?.stopEdit()
        discardTmpTileChanges()

        getNewItemVars()?.let { newItemVars ->
            currentTile?.let { tile ->
                sendEvent(Event.ActionController.AddAction(
                    ReplaceTileAction(tile) {
                        tile.modifyItemVars(currentTileItemIndex, if (newItemVars.isEmpty()) null else newItemVars)
                    }
                ))

                sendEvent(Event.Global.RefreshFrame())
            }
        }

        dispose()
    }

    private fun discardChangesAndDispose() {
        discardTmpTileChanges()
        dispose()
    }

    private fun dispose() {
        currentTile = null
        initialTileItemsId = null
        currentEditVar = null
        varsFilter.set("")
        isShowModifiedVars.set(false)
        variables.clear()
        sendEvent(Event.CanvasController.Block(CanvasBlockStatus(false)))
    }

    private fun handleOpen(event: Event<Pair<Tile, TileItemIdx>, Unit>) {
        sendEvent(Event.CanvasController.Block(CanvasBlockStatus(true)))
        WINDOW_ID++
        currentTile = event.body.first
        initialTileItemsId = event.body.first.getTileItemsId().clone()
        currentTileItemIndex = event.body.second
        collectVarsToDisplay()
    }

    private fun handleResetEnvironment() {
        dispose()
    }

    private fun handleSwitchMap() {
        discardChangesAndDispose()
    }

    private fun handleCloseMap() {
        dispose()
    }
}
