package strongdmm.event.type.service

import strongdmm.event.Event

abstract class TriggerPinnedVariablesService {
    class FetchPinnedVariables(callback: (Set<String>) -> Unit) : Event<Unit, Set<String>>(Unit, callback)
    class PinVariable(variableName: String) : Event<String, Unit>(variableName, null)
    class UnpinVariable(variableName: String) : Event<String, Unit>(variableName, null)
}
