package strongdmm.ui

import imgui.ImGui
import strongdmm.window.Window

object LayoutManager {
    const val ELEMENT_MARGIN: Float = 10f

    object Top {
        object Left {
            val posX: Float
                get() = ELEMENT_MARGIN
            val posY: Float
                get() = ImGui.getFrameHeight() + ELEMENT_MARGIN

            val width: Float
                get() = 330f * Window.pointSize
            val height: Float
                get() = Window.windowHeight * .6f - posY
        }
    }

    object Bottom {
        object Left {
            val posX: Float
                get() = Top.Left.posX
            val posY: Float
                get() = Top.Left.posY + Top.Left.height + ELEMENT_MARGIN

            val width: Float
                get() = Top.Left.width
            val height: Float
                get() = Window.windowHeight - posY - ELEMENT_MARGIN
        }

        object Right {
            val posX: Float
                get() = Window.windowWidth - width - ELEMENT_MARGIN
            val posY: Float
                get() = Window.windowHeight - height - ELEMENT_MARGIN

            val width: Float
                get() = 6.5f * ImGui.getFontSize()
            val height: Float
                get() = 2f * ImGui.getFontSize()
        }
    }
}
