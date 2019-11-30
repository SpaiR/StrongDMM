package strongdmm.window

import glm_.c
import glm_.f
import glm_.vec2.Vec2d
import imgui.*
import imgui.imgui.g
import imgui.impl.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Platform
import uno.glfw.GlfwCursor
import uno.glfw.GlfwWindow
import uno.glfw.glfw
import kotlin.collections.set

class GlfwWindow(
    private val window: GlfwWindow
) {
    init {
        with(ImGui.io) {
            // Setup back-end capabilities flags
            backendPlatformName = "imgui_impl_glfw"

            setClipboardTextFn = { glfwSetClipboardString(clipboardUserData, it) }
            getClipboardTextFn = { glfwGetClipboardString(clipboardUserData) }
            clipboardUserData = window.handle.L

            if (Platform.get() == Platform.WINDOWS) {
                imeWindowHandle = window.hwnd
            }

            mouseCursors[MouseCursor.Arrow.i] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR)
            mouseCursors[MouseCursor.TextInput.i] = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR)
            mouseCursors[MouseCursor.ResizeAll.i] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR)
            mouseCursors[MouseCursor.ResizeNS.i] = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR)
            mouseCursors[MouseCursor.ResizeEW.i] = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR)
            mouseCursors[MouseCursor.ResizeNESW.i] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR)
            mouseCursors[MouseCursor.ResizeNWSE.i] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR)
            mouseCursors[MouseCursor.Hand.i] = glfwCreateStandardCursor(GLFW_HAND_CURSOR)

            // Keyboard mapping. ImGui will use those indices to peek into the io.KeysDown[] array.
            keyMap[Key.Tab] = GLFW_KEY_TAB
            keyMap[Key.LeftArrow] = GLFW_KEY_LEFT
            keyMap[Key.RightArrow] = GLFW_KEY_RIGHT
            keyMap[Key.UpArrow] = GLFW_KEY_UP
            keyMap[Key.DownArrow] = GLFW_KEY_DOWN
            keyMap[Key.PageUp] = GLFW_KEY_PAGE_UP
            keyMap[Key.PageDown] = GLFW_KEY_PAGE_DOWN
            keyMap[Key.Home] = GLFW_KEY_HOME
            keyMap[Key.End] = GLFW_KEY_END
            keyMap[Key.Insert] = GLFW_KEY_INSERT
            keyMap[Key.Delete] = GLFW_KEY_DELETE
            keyMap[Key.Backspace] = GLFW_KEY_BACKSPACE
            keyMap[Key.Space] = GLFW_KEY_SPACE
            keyMap[Key.Enter] = GLFW_KEY_ENTER
            keyMap[Key.Escape] = GLFW_KEY_ESCAPE
            // keyMap[Key.KeyPadEnter] = GLFW_KEY_KP_ENTER
            keyMap[Key.A] = GLFW_KEY_A
            keyMap[Key.C] = GLFW_KEY_C
            keyMap[Key.V] = GLFW_KEY_V
            keyMap[Key.X] = GLFW_KEY_X
            keyMap[Key.Y] = GLFW_KEY_Y
            keyMap[Key.Z] = GLFW_KEY_Z

            // [JVM] Chain GLFW callbacks: our callbacks will be installed in parallel with any other already existing
            // native callbacks will be added at the GlfwWindow creation via default parameter
            window.mouseButtonCallbacks["imgui"] = { button: Int, action: Int, _: Int ->
                if (action == GLFW_PRESS && button in 0..2) {
                    mouseJustPressed[button] = true
                }
            }

            window.scrollCallbacks["imgui"] = { offset: Vec2d ->
                ImGui.io.mouseWheelH += offset.x.f
                ImGui.io.mouseWheel += offset.y.f
            }

            window.keyCallbacks["imgui"] = { key: Int, _: Int, action: Int, _: Int ->
                with(ImGui.io) {
                    if (key in keysDown.indices) {
                        when (action) {
                            GLFW_PRESS -> keysDown[key] = true
                            GLFW_RELEASE -> keysDown[key] = false
                        }
                    }

                    // Modifiers are not reliable across systems
                    keyCtrl = keysDown[GLFW_KEY_LEFT_CONTROL] || keysDown[GLFW_KEY_RIGHT_CONTROL]
                    keyShift = keysDown[GLFW_KEY_LEFT_SHIFT] || keysDown[GLFW_KEY_RIGHT_SHIFT]
                    keyAlt = keysDown[GLFW_KEY_LEFT_ALT] || keysDown[GLFW_KEY_RIGHT_ALT]
                    keySuper = keysDown[GLFW_KEY_LEFT_SUPER] || keysDown[GLFW_KEY_RIGHT_SUPER]
                }
            }

            window.charCallbacks["imgui"] = { c: Int -> if (!g.imeInProgress) ImGui.io.addInputCharacter(c.c) }
        }
    }

    fun newFrame() {
        // Setup display size (every frame to accommodate for window resizing)
        val size = window.size
        val displaySize = window.framebufferSize
        ImGui.io.displaySize put (window.size)

        if (size allGreaterThan 0) {
            ImGui.io.displayFramebufferScale put (displaySize / size)
        }

        // Setup time step
        val currentTime = glfw.time
        ImGui.io.deltaTime = if (time > 0) (currentTime - time).f else 1f / 60f
        time = currentTime

        updateMousePosAndButtons()
        updateMouseCursor()
    }

    fun shutdown() {
        mouseCursors.forEach(::glfwDestroyCursor)
        mouseCursors.fill(MemoryUtil.NULL)
        glfwTerminate()
        clientApi = GlfwClientApi.Unknown
    }

    private fun updateMousePosAndButtons() {
        // Update buttons
        repeat(ImGui.io.mouseDown.size) {
            // If a mouse press event came, always pass it as "mouse held this frame", so we don't miss click-release
            // events that are shorter than 1 frame.
            ImGui.io.mouseDown[it] = mouseJustPressed[it] || glfwGetMouseButton(window.handle.L, it) != 0
            mouseJustPressed[it] = false
        }

        // Update mouse position
        ImGui.io.mousePos put -Float.MAX_VALUE

        if (window.isFocused) {
            ImGui.io.mousePos put (window.cursorPos)
        }
    }

    private fun updateMouseCursor() {
        if (ImGui.io.configFlags has ConfigFlag.NoMouseCursorChange || window.cursorStatus == GlfwWindow.CursorStatus.Disabled) {
            return
        }

        val imguiCursor = ImGui.mouseCursor
        window.cursor = GlfwCursor(mouseCursors[imguiCursor.i].takeIf { it != MemoryUtil.NULL } ?: mouseCursors[MouseCursor.Arrow.i])
        window.cursorStatus = GlfwWindow.CursorStatus.Normal
    }
}
