package strongdmm.ui.vars

import imgui.ImBool
import imgui.ImGui.*
import imgui.ImString
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiMouseCursor
import imgui.enums.ImGuiStyleVar
import org.lwjgl.glfw.GLFW.*
import strongdmm.byond.*
import strongdmm.byond.dme.DmeItem
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.undoable.ReplaceTileAction
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.TileItemIdx
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.EventActionController
import strongdmm.event.type.controller.EventCanvasController
import strongdmm.event.type.controller.EventFrameController
import strongdmm.event.type.ui.EventEditVarsDialogUi
import strongdmm.event.type.ui.EventObjectPanelUi
import strongdmm.util.imgui.*
import strongdmm.window.AppWindow

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

    private var isFistOpen: Boolean = true

    private var currentTileItem: TileItem? = null // We can open edit menu with a tile item...
    private var currentTile: Tile? = null // ...or with a tile. If opened with the tile, then changes will be applied to a map.
    private var initialTileItemsId: LongArray? = null // Used to restore tile state if we didn't save our modified vars
    private var currentTileItemIndex: Int = 0 // This index is an item index inside of a Tile objects list
    private var currentEditVar: Var? = null

    // Variables filter buffer, resizable string
    private var varsFilter: ImString = ImString(FILTER_BUFFER).apply { inputData.isResizable = true }

    private val isShowModifiedVars: ImBool = ImBool(false)
    private val variables: MutableList<Var> = mutableListOf()
    private var variableInputFocused: Boolean = false // On first var select we will focus its input. Var is to check if we've already did it

    init {
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.OpenedMapChanged::class.java, ::handleOpenedMapChanged)
        consumeEvent(EventGlobal.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(EventEditVarsDialogUi.OpenWithTile::class.java, ::handleOpenWithTile)
        consumeEvent(EventEditVarsDialogUi.OpenWithTileItem::class.java, ::handleOpenWithTileItem)
    }

    fun process() {
        getTileItem()?.let { tileItem ->
            setNextWindowPos((AppWindow.windowWidth - DIALOG_WIDTH) / 2f, (AppWindow.windowHeight - DIALOG_HEIGHT) / 2f, ImGuiCond.Once)
            setNextWindowSize(DIALOG_WIDTH, DIALOG_HEIGHT, ImGuiCond.Once)

            window("Edit Variables: ${tileItem.type}##edit_variables_$WINDOW_ID") {
                drawControls()
                separator()
                child("vars_table") {
                    drawVariables()
                }
            }
        }
    }

    private fun drawControls() {
        checkbox("##is_show_modified_vars", isShowModifiedVars)
        setItemHoveredTooltip("Show modified variables")
        sameLine()
        setNextItemWidth(getWindowWidth() - 130f)

        if (isFistOpen) {
            setKeyboardFocusHere()
            isFistOpen = false
        }

        inputText("##vars_filter", varsFilter, "Variables Filter")
        sameLine()
        button("OK", block = ::saveChangesAndDispose)
        sameLine()
        button("Cancel", block = ::discardChangesAndDispose)
    }

    private fun drawVariables() {
        var rowCount = 0

        columns(2, "edit_vars_columns", true)

        for (variable in variables) {
            // Filtering when we need to show only modified vars
            if (isShowModifiedVars.get() && !(variable.isModified || variable.isChanged)) {
                continue
            }

            // Filtering when 'filter input' is not empty
            if (varsFilter.get().isNotEmpty() && !variable.name.contains(varsFilter.get())) {
                continue
            }

            alignTextToFramePadding()
            if (variable.isModified || variable.isChanged) {
                textColored(0f, 1f, 0f, 1f, variable.name)
            } else {
                text(variable.name)
            }

            nextColumn()

            if (variable === currentEditVar) {
                setNextItemWidth(getColumnWidth(-1))

                if (!variableInputFocused) {
                    setKeyboardFocusHere()
                    variableInputFocused = true
                }

                inputText("##${variable.name}", variable.buffer!!)

                if (isKeyPressed(GLFW_KEY_ENTER) || isKeyPressed(GLFW_KEY_KP_ENTER) || isKeyPressed(GLFW_KEY_ESCAPE)) {
                    currentEditVar?.stopEdit()
                    currentEditVar = null
                    applyTmpTileChanges()
                }
            } else {
                pushStyleColor(ImGuiCol.Button, 0)
                pushStyleColor(ImGuiCol.ButtonHovered, .25f, .58f, .98f, .5f)
                pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0f, 0f)

                button("${variable.value}##variable_btn_${rowCount++}", getColumnWidth(-1)) {
                    variableInputFocused = false

                    if (currentEditVar != null) {
                        currentEditVar!!.stopEdit()
                        applyTmpTileChanges()
                    }

                    currentEditVar = variable
                    variable.startEdit()
                }
                if (isItemHovered()) {
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

        // To collect vars from the dme hierarchy
        fun collectVars(dmeItem: DmeItem) {
            dmeItem.vars.filterKeys { variableName -> variableName !in HIDDEN_VARS }.forEach { (name, value) ->
                if (variables.none { variable -> variable.name == name }) {
                    variables.add(Var(name, value ?: "null", value ?: "null"))
                }
            }

            dmeItem.getParent()?.let { collectVars(it) }
        }

        // To collect vars from the current tile item
        getTileItem()?.let { tileItem ->
            tileItem.customVars?.filterKeys { variableName -> variableName !in HIDDEN_VARS }?.forEach { name, value ->
                variables.add(Var(name, value, tileItem.dmeItem.getVar(name) ?: "null"))
            }

            // After we got vars from the tile item we need to go though its dme representation and all children
            collectVars(tileItem.dmeItem)
        }

        variables.sortBy { it.name }
    }

    private fun getTileItem(): TileItem? {
        return currentTileItem ?: currentTile?.tileItems?.get(currentTileItemIndex)
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
            currentTile?.let {
                it.modifyItemVars(currentTileItemIndex, if (newItemVars.isEmpty()) null else newItemVars)
                sendEvent(EventFrameController.Refresh())
            }
        }
    }

    private fun discardTmpTileChanges() {
        if (currentTile != null && initialTileItemsId != null) {
            currentTile!!.replaceTileItemsId(initialTileItemsId!!)
            sendEvent(EventFrameController.Refresh())
        }
    }

    private fun saveChangesAndDispose() {
        currentEditVar?.stopEdit()
        discardTmpTileChanges()

        getNewItemVars()?.let { newItemVars ->
            if (currentTile != null) { // in case if we have a tile to apply changes
                sendEvent(
                    EventActionController.AddAction(
                        ReplaceTileAction(currentTile!!) {
                            currentTile!!.modifyItemVars(currentTileItemIndex, if (newItemVars.isEmpty()) null else newItemVars)
                        }
                    )
                )

                sendEvent(EventFrameController.Refresh())
            } else if (currentTileItem != null) { // if there is no tile, then we will ensure that new instance is created
                GlobalTileItemHolder.getOrCreate(currentTileItem!!.type, if (newItemVars.isEmpty()) null else newItemVars)
            }

            sendEvent(EventObjectPanelUi.Update())
        }

        dispose()
    }

    private fun discardChangesAndDispose() {
        discardTmpTileChanges()
        dispose()
    }

    private fun dispose() {
        currentTileItem = null
        currentTile = null
        initialTileItemsId = null
        currentEditVar = null
        varsFilter.set("")
        isShowModifiedVars.set(false)
        variables.clear()
        sendEvent(EventCanvasController.Block(false))
    }

    private fun open() {
        isFistOpen = true
        sendEvent(EventCanvasController.Block(true))
        WINDOW_ID++
    }

    private fun handleEnvironmentReset() {
        dispose()
    }

    private fun handleOpenedMapChanged() {
        discardChangesAndDispose()
    }

    private fun handleOpenedMapClosed() {
        dispose()
    }

    private fun handleOpenWithTile(event: Event<Pair<Tile, TileItemIdx>, Unit>) {
        open()
        currentTile = event.body.first
        initialTileItemsId = event.body.first.getTileItemsId().clone()
        currentTileItemIndex = event.body.second
        collectVarsToDisplay()
    }

    private fun handleOpenWithTileItem(event: Event<TileItem, Unit>) {
        open()
        currentTileItem = event.body
        collectVarsToDisplay()
    }
}
