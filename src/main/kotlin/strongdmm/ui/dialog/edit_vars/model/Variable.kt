package strongdmm.ui.dialog.edit_vars.model

import imgui.ImString

class Variable(
    val name: String,
    private val initialValue: String, // value currently used by tile item (taken from map)
    private val originalValue: String // value from dme item (taken from code)
) {
    val hash: Int = name.hashCode()

    // The value for current variable
    val value: ImString = ImString(initialValue).apply {
        inputData.isResizable = true
    }

    // Means that the value is not equal to the value parsed from the codebase
    var isModified: Boolean = initialValue != originalValue
        private set

    // Means that it is not equal to the value which it had on dialog creation
    var isChanged: Boolean = false
        private set

    var isPinned: Boolean = false

    fun stopEdit() {
        if (value.get().isBlank()) {
            value.set("null")
        }

        isModified = value.get() != originalValue
        isChanged = value.get() != initialValue
    }
}
