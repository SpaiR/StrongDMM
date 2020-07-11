package strongdmm.util.imgui

import imgui.flag.ImGuiMouseButton
import strongdmm.util.imgui.ext.WindowButton

object ImGuiExt {
    private val windowButton = WindowButton()

    fun windowButton(btnTxt: String, mouseBtn: Int = ImGuiMouseButton.Left, action: () -> Unit) = windowButton.render(btnTxt, mouseBtn, action)
}
