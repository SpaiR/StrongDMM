package io.github.spair.strongdmm.gui.view

import javax.swing.JComponent

fun JComponent.addAll(vararg components: JComponent): JComponent {
    components.forEach { add(it) }
    return this
}
