package strongdmm.ui.dialog.edit_vars

import strongdmm.byond.*
import strongdmm.byond.dme.DmeItem
import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerActionService
import strongdmm.event.type.service.TriggerFrameService
import strongdmm.event.type.service.TriggerPinnedVariablesService
import strongdmm.event.type.ui.TriggerObjectPanelUi
import strongdmm.service.action.undoable.ReplaceTileAction
import strongdmm.ui.dialog.edit_vars.model.Variable

class ViewController(
    private val state: State
) : EventHandler {
    companion object {
        private val hiddenVars: Set<String> = setOf(
            VAR_TYPE, VAR_PARENT_TYPE, VAR_VARS, VAR_X, VAR_Y, VAR_Z, VAR_CONTENTS, VAR_FILTERS, VAR_LOC, VAR_MAPTEXT,
            VAR_MAPTEXT_WIDTH, VAR_MAPTEXT_HEIGHT, VAR_MAPTEXT_X, VAR_MAPTEXT_Y, VAR_OVERLAYS, VAR_UNDERLAYS, VAR_VERBS,
            VAR_APPEARANCE, VAR_VIS_CONTENTS, VAR_VIS_LOCS
        )
    }

    fun doOk() {
        state.currentEditVar?.stopEdit()
        discardTmpTileChanges()

        getNewItemVars()?.let { newItemVars ->
            if (state.currentTile != null) { // in case if we have a tile to apply changes
                sendEvent(
                    TriggerActionService.AddAction(
                        ReplaceTileAction(state.currentTile!!) {
                            state.currentTile!!.modifyItemVars(state.currentTileItemIndex, if (newItemVars.isEmpty()) null else newItemVars)
                        }
                    )
                )

                sendEvent(TriggerFrameService.RefreshFrame())
            } else if (state.currentTileItem != null) { // if there is no tile, then we will ensure that new instance is created
                GlobalTileItemHolder.getOrCreate(state.currentTileItem!!.type, if (newItemVars.isEmpty()) null else newItemVars)
            }

            sendEvent(TriggerObjectPanelUi.Update())
        }

        dispose()
    }

    fun doCancel() {
        discardTmpTileChanges()
        dispose()
    }

    fun doPinVariable(variable: Variable) {
        variable.isPinned = !variable.isPinned

        if (variable.isPinned) {
            sendEvent(TriggerPinnedVariablesService.PinVariable(variable.name))
        } else {
            sendEvent(TriggerPinnedVariablesService.UnpinVariable(variable.name))
        }

        state.isDoUpdatePinnedVariables = true
    }

    fun doStartEdit(variable: Variable) {
        state.variableInputFocused = false

        if (state.currentEditVar != null) {
            state.currentEditVar!!.stopEdit()
            applyTmpTileChanges()
        }

        state.currentEditVar = variable
    }

    fun doStopEdit() {
        state.currentEditVar?.stopEdit()
        state.currentEditVar = null
        applyTmpTileChanges()
    }

    fun resetVariableToDefault(variable: Variable) {
        val defaultValue = getDefaultVariableValue(variable)
        if (defaultValue != variable.value.get()) {
            variable.value.set(defaultValue)
            variable.stopEdit()
            applyTmpTileChanges()
        }
    }

    fun getDefaultVariableValue(variable: Variable): String {
        return getTileItem()?.dmeItem?.getVar(variable.name) ?: "null"
    }

    fun open() {
        state.isFistOpen = true
        sendEvent(Reaction.ApplicationBlockChanged(true))
        state.windowId++
    }

    fun collectDisplayVariables() {
        fun getVariableType(dmeItem: DmeItem, variableName: String): String {
            val items = mutableListOf<DmeItem>()

            fun collectItemTree(item: DmeItem) {
                item.getParent()?.let { collectItemTree(it) }
                items.add(item)
            }

            collectItemTree(dmeItem)

            items.forEach {
                if (it.vars.containsKey(variableName)) {
                    return it.type
                }
            }

            return ""
        }

        // To collect vars from the dme hierarchy
        fun collectVarsFromEnvironment(dmeItem: DmeItem) {
            dmeItem.vars.filterKeys { variableName -> variableName !in hiddenVars }.forEach { (name, value) ->
                if (state.variables.none { variable -> variable.name == name }) {
                    state.variables.add(Variable(getVariableType(dmeItem, name), name, value ?: "null", value ?: "null"))
                }
            }

            dmeItem.getParent()?.let { collectVarsFromEnvironment(it) }
        }

        // To collect vars from the current tile item
        getTileItem()?.let { tileItem ->
            tileItem.customVars?.filterKeys { variableName -> variableName !in hiddenVars }?.forEach { name, value ->
                state.variables.add(Variable(getVariableType(tileItem.dmeItem, name), name, value, tileItem.dmeItem.getVar(name) ?: "null"))
            }

            // After we got vars from the tile item we need to go though its dme representation and all children
            collectVarsFromEnvironment(tileItem.dmeItem)
        }

        state.variables.sortBy { it.name }
    }

    fun collectVariablesByType() {
        state.variablesByType.putAll(state.variables.map { variable -> variable.type to state.variables.filter { variable.type == it.type } })
    }

    fun collectPinnedVariables() {
        sendEvent(TriggerPinnedVariablesService.FetchPinnedVariables { pinnedVariables ->
            state.variables.forEach { variable ->
                if (pinnedVariables.contains(variable.name)) {
                    variable.isPinned = true
                    state.pinnedVariables.add(variable)
                }
            }
        })
    }

    fun checkPinnedVariables() {
        if (state.isDoUpdatePinnedVariables) {
            state.isDoUpdatePinnedVariables = false
            state.pinnedVariables.clear()
            collectPinnedVariables()
        }
    }

    fun isFilteredOutType(type: String): Boolean {
        return state.isShowVarsByType.get() && state.typesFilter.length > 0 && !type.contains(state.typesFilter.get())
    }

    fun isFilteredOutVariable(variable: Variable): Boolean {
        // Filter when we need to show only modified vars
        if (state.isShowModifiedVars.get() && !(variable.isModified || variable.isChanged)) {
            return true
        }

        // Filter when 'filter input' is not empty
        if (state.varsFilter.length > 0 && !variable.name.contains(state.varsFilter.get())) {
            return true
        }

        return false
    }

    private fun applyTmpTileChanges() {
        getNewItemVars()?.let { newItemVars ->
            state.currentTile?.let {
                GlobalTileItemHolder.tmpOperation {
                    it.modifyItemVars(state.currentTileItemIndex, if (newItemVars.isEmpty()) null else newItemVars)
                }
                sendEvent(TriggerFrameService.RefreshFrame())
            }
        }
    }

    private fun discardTmpTileChanges() {
        if (state.currentTile != null && state.initialTileItemsId != null) {
            state.currentTile!!.replaceTileItemsId(state.initialTileItemsId!!)
            sendEvent(TriggerFrameService.RefreshFrame())
        }
    }

    private fun dispose() {
        state.currentTileItem = null
        state.currentTile = null
        state.initialTileItemsId = null
        state.currentEditVar = null
        state.varsFilter.set("")
        state.isShowModifiedVars.set(false)
        state.variables.clear()
        state.variablesByType.clear()
        state.pinnedVariables.clear()

        sendEvent(Reaction.ApplicationBlockChanged(false))
    }

    fun getTileItem(): TileItem? {
        return state.currentTileItem ?: state.currentTile?.tileItems?.get(state.currentTileItemIndex)
    }

    private fun getNewItemVars(): Map<String, String>? {
        if (state.variables.none { it.isChanged }) {
            return null
        }

        val newItemVars = mutableMapOf<String, String>()

        state.variables.filter { it.isChanged || it.isModified }.forEach {
            newItemVars[it.name] = it.value.get()
        }

        return newItemVars
    }
}
