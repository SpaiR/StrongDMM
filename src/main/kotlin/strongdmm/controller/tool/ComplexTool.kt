package strongdmm.controller.tool

abstract class ComplexTool : Tool() {
    open lateinit var currentTool: Tool

    override var isActive: Boolean
        get() = currentTool.isActive
        set(value) {
            currentTool.isActive = value
        }
}
