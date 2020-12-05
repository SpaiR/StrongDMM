package strongdmm.util.imgui.ext

import imgui.ImGui
import imgui.ImVec2
import strongdmm.util.imgui.COLOR_DIMGREY
import strongdmm.util.imgui.COLOR_WHITE
import strongdmm.application.window.Window

class WindowButton {
    companion object {
        private const val TOP_PADDING_CLIP: Int = 1
    }

    private val windowPos: ImVec2 = ImVec2()
    private val windowSize: ImVec2 = ImVec2()

    private val buttonTextSize: ImVec2 = ImVec2()

    fun render(btnTxt: String, mouseBtn: Int, action: () -> Unit) {
        ImGui.getWindowPos(windowPos)
        ImGui.getWindowSize(windowSize)
        ImGui.calcTextSize(buttonTextSize, btnTxt)

        val btnMinX = getButtonPosX()
        val btnMinY = getButtonPosY()
        val btnMaxX = btnMinX + buttonTextSize.x
        val btnMaxY = btnMinY + buttonTextSize.y

        val isHover = ImGui.isMouseHoveringRect(btnMinX, btnMinY, btnMaxX, btnMaxY, false)
        val btnColor = if (isHover) COLOR_WHITE else COLOR_DIMGREY

        ImGui.pushClipRect(btnMinX, btnMinY - TOP_PADDING_CLIP, btnMaxX, btnMaxY, false)
        ImGui.getWindowDrawList().addText(btnMinX, btnMinY, btnColor, btnTxt)
        ImGui.popClipRect()

        if (isHover && ImGui.isMouseClicked(mouseBtn)) {
            action()
        }
    }

    private fun getButtonPosX(): Float = windowPos.x + windowSize.x - getButtonRightPadding()
    private fun getButtonPosY(): Float = windowPos.y + getButtonTopPadding()

    private fun getButtonRightPadding(): Float = 16f * Window.pointSize
    private fun getButtonTopPadding(): Float = 2.5f * Window.pointSize
}
