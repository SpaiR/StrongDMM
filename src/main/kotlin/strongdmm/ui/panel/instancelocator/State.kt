package strongdmm.ui.panel.instancelocator

import imgui.type.ImBoolean
import imgui.type.ImInt
import imgui.type.ImString

class State {
    val doInstanceLocatorOpen: ImBoolean = ImBoolean(false)

    var isFirstOpen: Boolean = true

    val searchType: ImString = ImString(50).apply { inputData.isResizable = true }

    var mapMaxX: Int = 255
    var mapMaxY: Int = 255

    val searchX1: ImInt = ImInt(1)
    val searchY1: ImInt = ImInt(1)
    val searchX2: ImInt = ImInt(255)
    val searchY2: ImInt = ImInt(255)
}
