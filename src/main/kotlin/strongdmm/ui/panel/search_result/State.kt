package strongdmm.ui.panel.search_result

import imgui.type.ImBoolean
import imgui.type.ImString
import strongdmm.ui.panel.search_result.model.SearchPosition
import strongdmm.ui.panel.search_result.model.SearchResult

class State {
    val isOpen: ImBoolean = ImBoolean(false)

    var searchResult: SearchResult? = null
    val replaceType: ImString = ImString(50).apply { inputData.isResizable = true }
    var isReplaceEnabled: Boolean = false

    val positionsToRemove: MutableList<SearchPosition> = mutableListOf()
}
