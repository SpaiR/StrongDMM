package strongdmm.ui.panel.layers_filter

import gnu.trove.set.hash.TLongHashSet
import imgui.type.ImBoolean
import imgui.type.ImString
import strongdmm.byond.dme.Dme

class State {
    val isOpened: ImBoolean = ImBoolean(false)

    var currentEnvironment: Dme? = null
    val filteredTypesId: TLongHashSet = TLongHashSet()

    val typesFilter: ImString = ImString(50)
}
