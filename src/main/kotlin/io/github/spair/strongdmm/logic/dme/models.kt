package io.github.spair.strongdmm.logic.dme

data class Dme(val dmeItems: Map<String, DmeItem?>) {
    fun getItem(type: String) = dmeItems[type]
}

data class DmeItem(
    private val environment: Dme,
    val type: String,
    val vars: Map<String, String?>,
    val children: List<String>
) {

    private val lookedVars = mutableMapOf<String, String?>()

    val parentType: String by lazy {
        when (type) {
            TYPE_DATUM -> ""
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

        return if (parentType.isNotEmpty()) {
            val parent = environment.getItem(parentType)!!
            parent.getVar(name).apply { lookedVars[name] = this }
        } else {
            null
        }
    }

    fun getVarText(name: String) = getVar(name)?.run { substring(1, length - 1) }
    fun getVarFloat(name: String) = getVar(name)?.toFloatOrNull()
    fun getVarInt(name: String) = getVar(name)?.toIntOrNull()

    override fun toString(): String {
        return "DmeItem(type='$type', vars=$vars, children=$children)"
    }
}

fun isType(t1: String, t2: String): Boolean {
    return t1.isNotEmpty() && t2.isNotEmpty() && when (t1) {
        t2 -> true
        TYPE_DATUM -> false // Root type can only be type of itself
        TYPE_ATOM -> t2 == TYPE_DATUM
        TYPE_ATOM_MOVABLE, TYPE_AREA, TYPE_TURF -> t2 == TYPE_DATUM || t2 == TYPE_ATOM
        TYPE_OBJ, TYPE_MOB -> t2 == TYPE_DATUM || t2 == TYPE_ATOM || t2 == TYPE_ATOM_MOVABLE
        else -> isType(t1.substringBeforeLast('/'), t2)
    }
}
