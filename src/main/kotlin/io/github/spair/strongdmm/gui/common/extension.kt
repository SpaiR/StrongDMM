package io.github.spair.strongdmm.gui.common

import javax.swing.JComponent

fun JComponent.addAll(vararg components: JComponent): JComponent {
    components.forEach { add(it) }
    return this
}
