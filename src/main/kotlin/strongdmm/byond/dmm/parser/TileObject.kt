package strongdmm.byond.dmm.parser

class TileObject(
    val type: String
) {
    var vars: MutableMap<String, String>? = null
        private set

    fun setVars(vars: Map<String, String>?) {
        this.vars = vars?.toSortedMap()
    }

    fun putVar(name: String, value: String) {
        if (vars == null) {
            vars = sortedMapOf(name to value)
        } else {
            vars!![name] = value
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TileObject

        if (type != other.type) return false
        if (vars != other.vars) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (vars?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "TileObject(type='$type', vars=$vars)"
    }
}
