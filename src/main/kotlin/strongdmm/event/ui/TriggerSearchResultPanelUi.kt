package strongdmm.event.ui

import strongdmm.event.Event
import strongdmm.ui.panel.searchresult.model.SearchResult

abstract class TriggerSearchResultPanelUi {
    class Open(body: SearchResult) : Event<SearchResult, Unit>(body, null)
}
