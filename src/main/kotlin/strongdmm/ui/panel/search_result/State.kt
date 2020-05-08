package strongdmm.ui.panel.search_result

import imgui.ImBool
import imgui.ImString
import strongdmm.ui.panel.search_result.model.SearchPosition
import strongdmm.ui.panel.search_result.model.SearchResult

class State {
    val isOpen: ImBool = ImBool(false)

    var searchResult: SearchResult? = null
    val replaceType: ImString = ImString(50).apply { inputData.isResizable = true }
    var isReplaceEnabled: Boolean = false

    val positionsToRemove: MutableList<SearchPosition> = mutableListOf()
}
