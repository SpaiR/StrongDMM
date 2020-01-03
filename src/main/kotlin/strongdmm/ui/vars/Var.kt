package strongdmm.ui.vars

import imgui.ImString

class Var(
    val name: String,
    private val initialValue: String, // value currently used by tile item (taken from map)
    private val originalValue: String // value from dme item (taken from code)
) {

    var value: String = initialValue // The actual value for current variable
        private set
    var buffer: ImString? = null // Buffer is used by the ImGui to modify value of the variable
        private set
    var isModified: Boolean = value != originalValue // Means that the value is not equal to the value parsed from codebase
        private set
    var isChanged: Boolean = value != initialValue // Means that it is not equal to the value which it had on dialog creation
        private set

    fun startEdit() {
        buffer = ImString(value).apply { inputData.isResizable = true }
    }

    fun stopEdit() {
        value = buffer!!.get()

        if (value.isBlank()) {
            value = "null"
        }

        isModified = value != originalValue
        isChanged = value != initialValue
        buffer = null
    }
}
