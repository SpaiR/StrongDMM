package strongdmm.controller.environment

inline class EnvOpenStatus(val value: Boolean) {
    fun isOpen(): Boolean = value

    override fun toString(): String = "$value"
}
