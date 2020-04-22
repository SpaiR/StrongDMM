package strongdmm.ui.layers.filter.panel

import gnu.trove.set.hash.TLongHashSet
import imgui.ImBool
import imgui.ImString
import strongdmm.byond.dme.Dme

class State {
    val isOpened: ImBool = ImBool(false)

    var currentEnvironment: Dme? = null
    val filteredTypesId: TLongHashSet = TLongHashSet()

    val typesFilter: ImString = ImString(50)
}
