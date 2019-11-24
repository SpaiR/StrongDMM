package strongdmm.ui.vars

import imgui.internal.strlen

class Var(
    val name: String,
    private val initialValue: String, // value currently used by tile item (taken from map)
    private val originalValue: String // value from dme item (taken from code)
) {
    companion object {
        private const val BUFFER_SIZE: Int = 10000
        private val VALUE_TAIL: String = " ".repeat(1000) // Needed for GUI to make ImGui consider that item spreads through the line fully
    }

    var value: String = initialValue // The actual value for current variable
        private set
    var displayValue: String = value + VALUE_TAIL // Used to display in the dialog. Needed since it has a tail which consists of space chars.
        private set
    var buffer: CharArray? = null // Buffer is used by the ImGui to modify value of the variable
        private set
    var isModified: Boolean = value != originalValue // Means that the value is not equal to the value of parsed from a codebase
        private set
    val isChanged: Boolean // Means that the is not equal to the value which it had on a dialog creation
        get() = value != initialValue

    fun startEdit() {
        buffer = value.toCharArray().copyOf(BUFFER_SIZE)
    }

    fun stopEdit() {
        value = String(buffer!!, 0, buffer!!.strlen)

        if (value.isBlank()) {
            value = "null"
        }

        displayValue = value + VALUE_TAIL
        isModified = value != originalValue
        buffer = null
    }
}
