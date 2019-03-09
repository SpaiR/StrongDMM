package io.github.spair.strongdmm.gui.controller

interface Controller {
    fun init()
}

// Logic behind this class is that one controller should have reference to one specific view.
// It is possible to add property with injected view by hands, but this should be avoided.
abstract class ViewController<T>(protected val view: T) : Controller
