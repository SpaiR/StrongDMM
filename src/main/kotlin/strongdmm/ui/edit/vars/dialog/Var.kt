package strongdmm.ui.edit.vars.dialog

import imgui.internal.strlen

class Var(
    val name: String,
    private val initialValue: String,
    private val originalValue: String
) {
    companion object {
        private const val BUFFER_SIZE: Int = 10000
        private val VALUE_TAIL: String = " ".repeat(1000)  // Needed for GUI to make ImGui consider that item spreads through the line fully
    }

    var value: String = initialValue
        private set
    var visibleValue: String = value + VALUE_TAIL
        private set
    var buffer: CharArray? = null
        private set
    var isModified: Boolean = value != originalValue
        private set
    val isChanged: Boolean
        get() = value != initialValue

    fun startEdit() {
        buffer = value.toCharArray().copyOf(BUFFER_SIZE)
    }

    fun stopEdit() {
        value = String(buffer!!, 0, buffer!!.strlen)

        if (value.isBlank()) {
            value = "null"
        }

        visibleValue = value + VALUE_TAIL
        isModified = value != originalValue
        buffer = null
    }
}
