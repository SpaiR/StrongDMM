package io.github.spair.strongdmm.gui.common

import javax.swing.border.Border
import javax.swing.border.EmptyBorder

object BorderUtil {

    fun createEmptyBorder(top: Int = 0, left: Int = 0, bottom: Int = 0, right: Int = 0): Border {
        return EmptyBorder(top, left, bottom, right)
    }

    fun createEmptyBorder(side: Int = 0): Border {
        return createEmptyBorder(side, side, side, side)
    }
}
