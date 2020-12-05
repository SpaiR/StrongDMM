package strongdmm.application.window

import imgui.ImGui
import imgui.ImGui.styleColorsDark
import imgui.ImVec2
import imgui.flag.ImGuiCol

object AppWindowStyle {
    private val defaultFrameRounding: Float
    private val defaultGrabRounding: Float
    private val defaultWindowRounding: Float
    private val defaultChildRounding: Float
    private val defaultScrollbarRounding: Float
    private val defaultWindowBorderSize: Float
    private val defaultPopupBorderSize: Float
    private val defaultWindowTitleAlign: ImVec2 = ImVec2()

    init {
        ImGui.getStyle().apply {
            defaultFrameRounding = frameRounding
            defaultGrabRounding = grabRounding
            defaultWindowRounding = windowRounding
            defaultChildRounding = childRounding
            defaultScrollbarRounding = scrollbarRounding
            defaultWindowBorderSize = windowBorderSize
            defaultPopupBorderSize = popupBorderSize
            getWindowTitleAlign(defaultWindowTitleAlign)
        }
    }

    fun setPeacefulSpace() {
        ImGui.getStyle().apply {
            restoreDefault()

            frameRounding = 2f
            grabRounding = 2f

            setColor(ImGuiCol.Text, 0.95f, 0.96f, 0.98f, 1.00f)
            setColor(ImGuiCol.TextDisabled, 0.36f, 0.42f, 0.47f, 1.00f)
            setColor(ImGuiCol.WindowBg, 0.11f, 0.15f, 0.17f, 1.00f)
            setColor(ImGuiCol.ChildBg, 0.11f, 0.15f, 0.17f, 1.00f)
            setColor(ImGuiCol.PopupBg, 0.08f, 0.08f, 0.08f, 0.94f)
            setColor(ImGuiCol.Border, 0.08f, 0.10f, 0.12f, 1.00f)
            setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f)
            setColor(ImGuiCol.FrameBg, 0.20f, 0.25f, 0.29f, 1.00f)
            setColor(ImGuiCol.FrameBgHovered, 0.12f, 0.20f, 0.28f, 1.00f)
            setColor(ImGuiCol.FrameBgActive, 0.09f, 0.12f, 0.14f, 1.00f)
            setColor(ImGuiCol.TitleBg, 0.09f, 0.12f, 0.14f, 0.65f)
            setColor(ImGuiCol.TitleBgActive, 0.08f, 0.10f, 0.12f, 1.00f)
            setColor(ImGuiCol.TitleBgCollapsed, 0.00f, 0.00f, 0.00f, 0.51f)
            setColor(ImGuiCol.MenuBarBg, 0.15f, 0.18f, 0.22f, 1.00f)
            setColor(ImGuiCol.ScrollbarBg, 0.02f, 0.02f, 0.02f, 0.39f)
            setColor(ImGuiCol.ScrollbarGrab, 0.20f, 0.25f, 0.29f, 1.00f)
            setColor(ImGuiCol.ScrollbarGrabHovered, 0.18f, 0.22f, 0.25f, 1.00f)
            setColor(ImGuiCol.ScrollbarGrabActive, 0.09f, 0.21f, 0.31f, 1.00f)
            setColor(ImGuiCol.CheckMark, 0.28f, 0.56f, 1.00f, 1.00f)
            setColor(ImGuiCol.SliderGrab, 0.28f, 0.56f, 1.00f, 1.00f)
            setColor(ImGuiCol.SliderGrabActive, 0.37f, 0.61f, 1.00f, 1.00f)
            setColor(ImGuiCol.Button, 0.20f, 0.25f, 0.29f, 1.00f)
            setColor(ImGuiCol.ButtonHovered, 0.28f, 0.56f, 1.00f, 1.00f)
            setColor(ImGuiCol.ButtonActive, 0.06f, 0.53f, 0.98f, 1.00f)
            setColor(ImGuiCol.Header, 0.20f, 0.25f, 0.29f, 0.55f)
            setColor(ImGuiCol.HeaderHovered, 0.26f, 0.59f, 0.98f, 0.80f)
            setColor(ImGuiCol.HeaderActive, 0.26f, 0.59f, 0.98f, 1.00f)
            setColor(ImGuiCol.Separator, 0.20f, 0.25f, 0.29f, 1.00f)
            setColor(ImGuiCol.SeparatorHovered, 0.10f, 0.40f, 0.75f, 0.78f)
            setColor(ImGuiCol.SeparatorActive, 0.10f, 0.40f, 0.75f, 1.00f)
            setColor(ImGuiCol.ResizeGrip, 0.26f, 0.59f, 0.98f, 0.25f)
            setColor(ImGuiCol.ResizeGripHovered, 0.26f, 0.59f, 0.98f, 0.67f)
            setColor(ImGuiCol.ResizeGripActive, 0.26f, 0.59f, 0.98f, 0.95f)
            setColor(ImGuiCol.Tab, 0.11f, 0.15f, 0.17f, 1.00f)
            setColor(ImGuiCol.TabHovered, 0.26f, 0.59f, 0.98f, 0.80f)
            setColor(ImGuiCol.TabActive, 0.20f, 0.25f, 0.29f, 1.00f)
            setColor(ImGuiCol.TabUnfocused, 0.11f, 0.15f, 0.17f, 1.00f)
            setColor(ImGuiCol.TabUnfocusedActive, 0.11f, 0.15f, 0.17f, 1.00f)
            setColor(ImGuiCol.PlotLines, 0.61f, 0.61f, 0.61f, 1.00f)
            setColor(ImGuiCol.PlotLinesHovered, 1.00f, 0.43f, 0.35f, 1.00f)
            setColor(ImGuiCol.PlotHistogram, 0.90f, 0.70f, 0.00f, 1.00f)
            setColor(ImGuiCol.PlotHistogramHovered, 1.00f, 0.60f, 0.00f, 1.00f)
            setColor(ImGuiCol.TextSelectedBg, 0.26f, 0.59f, 0.98f, 0.35f)
            setColor(ImGuiCol.DragDropTarget, 1.00f, 1.00f, 0.00f, 0.90f)
            setColor(ImGuiCol.NavHighlight, 0.26f, 0.59f, 0.98f, 1.00f)
            setColor(ImGuiCol.NavWindowingHighlight, 1.00f, 1.00f, 1.00f, 0.70f)
            setColor(ImGuiCol.NavWindowingDimBg, 0.80f, 0.80f, 0.80f, 0.20f)
            setColor(ImGuiCol.ModalWindowDimBg, 0.80f, 0.80f, 0.80f, 0.35f)
        }
    }

    fun setUnrealDarkness() {
        ImGui.getStyle().apply {
            restoreDefault()

            setColor(ImGuiCol.Text, 1.00f, 1.00f, 1.00f, 1.00f)
            setColor(ImGuiCol.TextDisabled, 0.50f, 0.50f, 0.50f, 1.00f)
            setColor(ImGuiCol.WindowBg, 0.06f, 0.06f, 0.06f, 0.94f)
            setColor(ImGuiCol.ChildBg, 1.00f, 1.00f, 1.00f, 0.00f)
            setColor(ImGuiCol.PopupBg, 0.08f, 0.08f, 0.08f, 0.94f)
            setColor(ImGuiCol.Border, 0.43f, 0.43f, 0.50f, 0.50f)
            setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f)
            setColor(ImGuiCol.FrameBg, 0.20f, 0.21f, 0.22f, 0.54f)
            setColor(ImGuiCol.FrameBgHovered, 0.40f, 0.40f, 0.40f, 0.40f)
            setColor(ImGuiCol.FrameBgActive, 0.18f, 0.18f, 0.18f, 0.67f)
            setColor(ImGuiCol.TitleBg, 0.04f, 0.04f, 0.04f, 1.00f)
            setColor(ImGuiCol.TitleBgActive, 0.29f, 0.29f, 0.29f, 1.00f)
            setColor(ImGuiCol.TitleBgCollapsed, 0.00f, 0.00f, 0.00f, 0.51f)
            setColor(ImGuiCol.MenuBarBg, 0.14f, 0.14f, 0.14f, 1.00f)
            setColor(ImGuiCol.ScrollbarBg, 0.02f, 0.02f, 0.02f, 0.53f)
            setColor(ImGuiCol.ScrollbarGrab, 0.31f, 0.31f, 0.31f, 1.00f)
            setColor(ImGuiCol.ScrollbarGrabHovered, 0.41f, 0.41f, 0.41f, 1.00f)
            setColor(ImGuiCol.ScrollbarGrabActive, 0.51f, 0.51f, 0.51f, 1.00f)
            setColor(ImGuiCol.CheckMark, 0.94f, 0.94f, 0.94f, 1.00f)
            setColor(ImGuiCol.SliderGrab, 0.51f, 0.51f, 0.51f, 1.00f)
            setColor(ImGuiCol.SliderGrabActive, 0.86f, 0.86f, 0.86f, 1.00f)
            setColor(ImGuiCol.Button, 0.44f, 0.44f, 0.44f, 0.40f)
            setColor(ImGuiCol.ButtonHovered, 0.46f, 0.47f, 0.48f, 1.00f)
            setColor(ImGuiCol.ButtonActive, 0.42f, 0.42f, 0.42f, 1.00f)
            setColor(ImGuiCol.Header, 0.70f, 0.70f, 0.70f, 0.31f)
            setColor(ImGuiCol.HeaderHovered, 0.70f, 0.70f, 0.70f, 0.80f)
            setColor(ImGuiCol.HeaderActive, 0.48f, 0.50f, 0.52f, 1.00f)
            setColor(ImGuiCol.Separator, 0.43f, 0.43f, 0.50f, 0.50f)
            setColor(ImGuiCol.SeparatorHovered, 0.72f, 0.72f, 0.72f, 0.78f)
            setColor(ImGuiCol.SeparatorActive, 0.51f, 0.51f, 0.51f, 1.00f)
            setColor(ImGuiCol.ResizeGrip, 0.91f, 0.91f, 0.91f, 0.25f)
            setColor(ImGuiCol.ResizeGripHovered, 0.81f, 0.81f, 0.81f, 0.67f)
            setColor(ImGuiCol.ResizeGripActive, 0.46f, 0.46f, 0.46f, 0.95f)
            setColor(ImGuiCol.PlotLines, 0.61f, 0.61f, 0.61f, 1.00f)
            setColor(ImGuiCol.PlotLinesHovered, 1.00f, 0.43f, 0.35f, 1.00f)
            setColor(ImGuiCol.PlotHistogram, 0.73f, 0.60f, 0.15f, 1.00f)
            setColor(ImGuiCol.PlotHistogramHovered, 1.00f, 0.60f, 0.00f, 1.00f)
            setColor(ImGuiCol.TextSelectedBg, 0.87f, 0.87f, 0.87f, 0.35f)
            setColor(ImGuiCol.DragDropTarget, 1.00f, 1.00f, 0.00f, 0.90f)
            setColor(ImGuiCol.NavHighlight, 0.60f, 0.60f, 0.60f, 1.00f)
            setColor(ImGuiCol.NavWindowingHighlight, 1.00f, 1.00f, 1.00f, 0.70f)
        }
    }

    fun setCrimsonMoon() {
        ImGui.getStyle().apply {
            frameRounding = 4.0f
            grabRounding = 4.0f
            windowBorderSize = 0.0f
            popupBorderSize = 0.0f

            setColor(ImGuiCol.Text, 1.00f, 1.00f, 1.00f, 1.00f)
            setColor(ImGuiCol.TextDisabled, 0.73f, 0.75f, 0.74f, 1.00f)
            setColor(ImGuiCol.WindowBg, 0.09f, 0.09f, 0.09f, 0.94f)
            setColor(ImGuiCol.ChildBg, 0.00f, 0.00f, 0.00f, 0.00f)
            setColor(ImGuiCol.PopupBg, 0.08f, 0.08f, 0.08f, 0.94f)
            setColor(ImGuiCol.Border, 0.20f, 0.20f, 0.20f, 0.50f)
            setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f)
            setColor(ImGuiCol.FrameBg, 0.71f, 0.39f, 0.39f, 0.54f)
            setColor(ImGuiCol.FrameBgHovered, 0.84f, 0.66f, 0.66f, 0.40f)
            setColor(ImGuiCol.FrameBgActive, 0.84f, 0.66f, 0.66f, 0.67f)
            setColor(ImGuiCol.TitleBg, 0.47f, 0.22f, 0.22f, 0.67f)
            setColor(ImGuiCol.TitleBgActive, 0.47f, 0.22f, 0.22f, 1.00f)
            setColor(ImGuiCol.TitleBgCollapsed, 0.47f, 0.22f, 0.22f, 0.67f)
            setColor(ImGuiCol.MenuBarBg, 0.34f, 0.16f, 0.16f, 1.00f)
            setColor(ImGuiCol.ScrollbarBg, 0.02f, 0.02f, 0.02f, 0.53f)
            setColor(ImGuiCol.ScrollbarGrab, 0.31f, 0.31f, 0.31f, 1.00f)
            setColor(ImGuiCol.ScrollbarGrabHovered, 0.41f, 0.41f, 0.41f, 1.00f)
            setColor(ImGuiCol.ScrollbarGrabActive, 0.51f, 0.51f, 0.51f, 1.00f)
            setColor(ImGuiCol.CheckMark, 1.00f, 1.00f, 1.00f, 1.00f)
            setColor(ImGuiCol.SliderGrab, 0.71f, 0.39f, 0.39f, 1.00f)
            setColor(ImGuiCol.SliderGrabActive, 0.84f, 0.66f, 0.66f, 1.00f)
            setColor(ImGuiCol.Button, 0.47f, 0.22f, 0.22f, 0.65f)
            setColor(ImGuiCol.ButtonHovered, 0.71f, 0.39f, 0.39f, 0.65f)
            setColor(ImGuiCol.ButtonActive, 0.20f, 0.20f, 0.20f, 0.50f)
            setColor(ImGuiCol.Header, 0.71f, 0.39f, 0.39f, 0.54f)
            setColor(ImGuiCol.HeaderHovered, 0.84f, 0.66f, 0.66f, 0.65f)
            setColor(ImGuiCol.HeaderActive, 0.84f, 0.66f, 0.66f, 0.00f)
            setColor(ImGuiCol.Separator, 0.43f, 0.43f, 0.50f, 0.50f)
            setColor(ImGuiCol.SeparatorHovered, 0.71f, 0.39f, 0.39f, 0.54f)
            setColor(ImGuiCol.SeparatorActive, 0.71f, 0.39f, 0.39f, 0.54f)
            setColor(ImGuiCol.ResizeGrip, 0.71f, 0.39f, 0.39f, 0.54f)
            setColor(ImGuiCol.ResizeGripHovered, 0.84f, 0.66f, 0.66f, 0.66f)
            setColor(ImGuiCol.ResizeGripActive, 0.84f, 0.66f, 0.66f, 0.66f)
            setColor(ImGuiCol.Tab, 0.71f, 0.39f, 0.39f, 0.54f)
            setColor(ImGuiCol.TabHovered, 0.84f, 0.66f, 0.66f, 0.66f)
            setColor(ImGuiCol.TabActive, 0.84f, 0.66f, 0.66f, 0.66f)
            setColor(ImGuiCol.TabUnfocused, 0.07f, 0.10f, 0.15f, 0.97f)
            setColor(ImGuiCol.TabUnfocusedActive, 0.14f, 0.26f, 0.42f, 1.00f)
            setColor(ImGuiCol.PlotLines, 0.61f, 0.61f, 0.61f, 1.00f)
            setColor(ImGuiCol.PlotLinesHovered, 1.00f, 0.43f, 0.35f, 1.00f)
            setColor(ImGuiCol.PlotHistogram, 0.90f, 0.70f, 0.00f, 1.00f)
            setColor(ImGuiCol.PlotHistogramHovered, 1.00f, 0.60f, 0.00f, 1.00f)
            setColor(ImGuiCol.TextSelectedBg, 0.26f, 0.59f, 0.98f, 0.35f)
            setColor(ImGuiCol.DragDropTarget, 1.00f, 1.00f, 0.00f, 0.90f)
            setColor(ImGuiCol.NavHighlight, 0.41f, 0.41f, 0.41f, 1.00f)
            setColor(ImGuiCol.NavWindowingHighlight, 1.00f, 1.00f, 1.00f, 0.70f)
            setColor(ImGuiCol.NavWindowingDimBg, 0.80f, 0.80f, 0.80f, 0.20f)
            setColor(ImGuiCol.ModalWindowDimBg, 0.80f, 0.80f, 0.80f, 0.35f)
        }
    }

    fun setDarkCoast() {
        ImGui.getStyle().apply {
            restoreDefault()
            styleColorsDark()
        }
    }

    private fun restoreDefault() {
        ImGui.getStyle().apply {
            frameRounding = defaultFrameRounding
            grabRounding = defaultGrabRounding
            windowRounding = defaultWindowRounding
            childRounding = defaultChildRounding
            scrollbarRounding = defaultScrollbarRounding
            windowBorderSize = defaultWindowBorderSize
            popupBorderSize = defaultPopupBorderSize
            setWindowTitleAlign(defaultWindowTitleAlign.x, defaultWindowTitleAlign.y)
        }
    }
}
