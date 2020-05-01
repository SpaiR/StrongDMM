package strongdmm.event.type.ui

import strongdmm.event.Event
import strongdmm.ui.panel.search_result.model.SearchResult

abstract class TriggerSearchResultPanelUi {
    class Open(body: SearchResult) : Event<SearchResult, Unit>(body, null)
}
