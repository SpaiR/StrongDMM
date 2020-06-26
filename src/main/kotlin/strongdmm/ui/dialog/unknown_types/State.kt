package strongdmm.ui.dialog.unknown_types

import imgui.type.ImString
import strongdmm.byond.dme.Dme
import strongdmm.event.Event
import strongdmm.service.map.UnknownType

class State {
    var isDoOpen: Boolean = false
    var currentEnvironment: Dme? = null
    var eventToReply: Event<Set<UnknownType>, Unit>? = null

    val inputStr: ImString = ImString().apply { inputData.isResizable = true }

    var unknownTypes: Set<UnknownType> = emptySet()

    var hasUnknownTypes: Boolean = false
    var variableToRemove: Pair<UnknownType, UnknownType.Variable>? = null
}
