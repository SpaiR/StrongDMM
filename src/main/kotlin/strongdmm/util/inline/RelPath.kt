package strongdmm.util.inline

inline class RelPath(val value: String) {
    companion object {
        val NONE: RelPath = RelPath("")
    }

    override fun toString(): String = value
}
