package io.github.spair.strongdmm.logic.dme

class Dme(val dmeItems: Map<String, DmeItem?>) {
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

fun isType(t1: String, t2: String): Boolean {
    return if (t1.isEmpty() || t2.isEmpty()) false
    else if (t1 == t2) true
    else if (t1 == TYPE_DATUM) false // Root type can only be type of itself
    else if (t1 == TYPE_ATOM) t2 == TYPE_DATUM
    else if (t1 == TYPE_ATOM_MOVABLE || t1 == TYPE_AREA || t1 == TYPE_TURF) t2 == TYPE_DATUM || t2 == TYPE_ATOM
    else if (t1 == TYPE_OBJ || t1 == TYPE_MOB) t2 == TYPE_DATUM || t2 == TYPE_ATOM || t2 == TYPE_ATOM_MOVABLE
    else t1.startsWith(t2)
}
