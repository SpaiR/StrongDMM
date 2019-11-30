package strongdmm.controller.canvas

inline class CanvasBlockStatus(val value: Boolean) {
    fun isBlocked(): Boolean = value

    override fun toString(): String = "$value"
}
