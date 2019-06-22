package io.github.spair.strongdmm.logic.dme

const val NON_EXISTENT_FLOAT: Float = -99999999999999999999999999999999999999f
const val NON_EXISTENT_INT: Int = -999999999

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

    val parent
        get() = parentType?.let { environment.getItem(it) }

    private val parentType by lazy {
        when (type) {
            TYPE_DATUM -> null
            TYPE_ATOM -> TYPE_DATUM
            TYPE_ATOM_MOVABLE, TYPE_AREA, TYPE_TURF -> TYPE_ATOM
            TYPE_OBJ, TYPE_MOB -> TYPE_ATOM_MOVABLE
            else -> type.substringBeforeLast('/')
        }
    }

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

    fun getVarFloat(name: String): Float {
        return try {
            getVar(name)?.toFloat() ?: NON_EXISTENT_FLOAT
        } catch (e: NumberFormatException) {
            NON_EXISTENT_FLOAT
        }
    }

    fun getVarInt(name: String): Int {
        return try {
            getVar(name)?.toInt() ?: NON_EXISTENT_INT
        } catch (e: NumberFormatException) {
            NON_EXISTENT_INT
        }
    }

    override fun toString(): String {
        return "DmeItem(type='$type', vars=$vars, children=$children)"
    }
}

// This 'isType' doesn't handle datum/atom and so on, since map editor doesn't place those types on the map,
// so additional checks would result into obsolete overhead.
fun isType(t1: String, t2: String) = t1.startsWith(t2)
