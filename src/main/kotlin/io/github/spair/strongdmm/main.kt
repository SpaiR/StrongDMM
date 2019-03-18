package io.github.spair.strongdmm

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.common.LeftScreenView
import io.github.spair.strongdmm.gui.common.RightScreenView
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasController
import io.github.spair.strongdmm.gui.menubar.MenuBarController
import io.github.spair.strongdmm.gui.objtree.ObjectTreeController
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasView
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.render.FrameRenderer
import io.github.spair.strongdmm.logic.render.RenderInstanceProvider
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton

// Entry point
fun main() {
    primaryFrame()
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
    bind() from singleton { MenuBarController() }
    bind() from singleton { ObjectTreeController() }
    bind() from singleton { MapCanvasController() }

    // Logic
    bind() from singleton { Environment() }
    bind() from singleton { DmiProvider() }
    bind() from singleton { FrameRenderer() }
    bind() from singleton { RenderInstanceProvider() }
}

fun primaryFrame() = DI.direct.instance<PrimaryFrame>()
