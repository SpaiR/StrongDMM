package io.github.spair.strongdmm.logic.dme

class Dme(private val dmeItems: Map<String, DmeItem?>) {
    fun getItem(type: String) = dmeItems[type]
}

class DmeItem(
    private val environment: Dme,
    val type: String,
    val vars: Map<String, String>,
    val children: List<String>
    ) {

    private val lookedVars = mutableMapOf<String, String?>()

    val parent get() = parentType?.let { environment.getItem(it) }

    val parentType by lazy {
        when (type) {
            TYPE_DATUM -> null
            TYPE_ATOM -> TYPE_DATUM
            TYPE_ATOM_MOVABLE, TYPE_AREA, TYPE_TURF -> TYPE_ATOM
            TYPE_OBJ, TYPE_MOB ->  TYPE_ATOM_MOVABLE
            else -> type.substringBeforeLast('/')
        }
    }

    fun isType(type: String) = isType(this.type, type)

    fun getVar(name: String): String? {
        if (vars.containsKey(name)) {
            return vars[name]
        } else if (lookedVars.containsKey(name)) {
            return lookedVars[name]
        }

        return parentType?.let {
            val parent = environment.getItem(it)!!
            parent.getVar(name).apply { lookedVars[name] = this }
        }
    }

    fun getVarText(name: String) = getVar(name)?.takeIf { it.isNotEmpty() }?.run { substring(1, length - 1) }
    fun getVarFloat(name: String) = getVar(name)?.toFloatOrNull()
    fun getVarInt(name: String) = getVar(name)?.toIntOrNull()

    override fun toString(): String {
        return "DmeItem(type='$type', vars=$vars, children=$children)"
    }
}
