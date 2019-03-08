package io.github.spair.strongdmm

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.controller.MenuBarController
import io.github.spair.strongdmm.gui.view.*
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.erased.bind
import org.kodein.di.erased.eagerSingleton
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton

// Entry point
fun main() {
    kodein.direct.instance<PrimaryFrame>().init()
}

// Application DI context
val kodein = Kodein {
    bind() from singleton { PrimaryFrame() }

    // Subviews
    bind() from singleton { MenuBarView() }
    bind() from singleton { LeftScreenView() }
    bind() from singleton { ObjectTreeView() }
    bind() from singleton { RightScreenView() }

    // Controllers
    bind<MenuBarController>() with singleton { MenuBarController() }
}
