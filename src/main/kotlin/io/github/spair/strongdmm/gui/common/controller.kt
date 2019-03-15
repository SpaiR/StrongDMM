package io.github.spair.strongdmm.gui.common

interface Controller {
    fun init()
}

// Logic behind this class is that one controller should have reference to one specific view.
// It is possible to add property with injected view by hands, but this should be avoided,
// since communication through controllers is preferable.
abstract class ViewController<T>(protected val view: T) : Controller
