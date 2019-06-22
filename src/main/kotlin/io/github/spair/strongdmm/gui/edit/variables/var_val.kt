package io.github.spair.strongdmm.gui.edit.variables

class Var(val name: Val, val value: Val)

interface Val {
    fun get(): String
    fun isInstanceVar(): Boolean
}

abstract class StrVal(private val isInstanceVar: Boolean) : Val {
    override fun toString(): String = get()
    override fun isInstanceVar(): Boolean = isInstanceVar
}

class VarName(private val name: String, isInstanceVar: Boolean = false) : StrVal(isInstanceVar) {
    override fun get(): String = name
}

class VarValue(private val value: String, isInstanceVar: Boolean = false) : StrVal(isInstanceVar) {
    override fun get(): String = value
}
