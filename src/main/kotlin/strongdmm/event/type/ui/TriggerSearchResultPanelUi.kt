package strongdmm.event.type.ui

import strongdmm.event.Event
import strongdmm.ui.search.SearchResult

abstract class TriggerSearchResultPanelUi {
    class Open(body: SearchResult) : Event<SearchResult, Unit>(body, null)
}
