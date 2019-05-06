package io.github.spair.strongdmm

import io.github.spair.strongdmm.gui.PrimaryFrame
import io.github.spair.strongdmm.gui.TabbedMapPanelView
import io.github.spair.strongdmm.gui.TabbedObjectPanelView
import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasView
import io.github.spair.strongdmm.gui.menubar.MenuBarView
import io.github.spair.strongdmm.gui.objtree.ObjectTreeView
import io.github.spair.strongdmm.logic.dmi.DmiProvider
import io.github.spair.strongdmm.logic.render.RenderInstanceProvider
import io.github.spair.strongdmm.logic.render.VisualComposer
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.erased.allInstances
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton

// Entry point
fun main() {
    PrimaryFrame.init()
}

// Application DI context
val DI = Kodein {
    // Views
    bind() from singleton { MenuBarView() }
    bind() from singleton { TabbedObjectPanelView() }
    bind() from singleton { TabbedMapPanelView() }
    bind() from singleton { ObjectTreeView() }
    bind() from singleton { InstanceListView() }
    bind() from singleton { MapCanvasView() }

    // Logic
    bind() from singleton { DmiProvider() }
    bind() from singleton { VisualComposer() }
    bind() from singleton { RenderInstanceProvider() }
}

inline fun <reified T : Any> diDirectAll() = DI.direct.allInstances<T>()
inline fun <reified T : Any> diInstance() = DI.instance<T>()
