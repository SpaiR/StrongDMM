package io.github.spair.strongdmm

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.controller.MapCanvasViewController
import io.github.spair.strongdmm.gui.controller.MenuBarViewController
import io.github.spair.strongdmm.gui.controller.ObjectTreeViewController
import io.github.spair.strongdmm.gui.view.*
import io.github.spair.strongdmm.logic.Environment
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton

// Entry point
fun main() {
    DI.direct.instance<PrimaryFrame>().init()
}

// Application DI context
val DI = Kodein {
    bind() from singleton { PrimaryFrame() }

    // Subviews
    bind() from singleton { MenuBarView() }
    bind() from singleton { LeftScreenView() }
    bind() from singleton { ObjectTreeView() }
    bind() from singleton { RightScreenView() }
    bind() from singleton { MapCanvasView() }

    // Controllers
    bind() from singleton { MenuBarViewController() }
    bind() from singleton { ObjectTreeViewController() }
    bind() from singleton { MapCanvasViewController() }

    // Logic
    bind() from singleton { Environment() }
}
