package strongdmm.window

import imgui.ImFontConfig
import imgui.ImGui
import imgui.ImGuiFreeType
import imgui.callbacks.ImStrConsumer
import imgui.callbacks.ImStrSupplier
import imgui.enums.*
import imgui.gl3.ImGuiImplGl3
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import strongdmm.util.icons.ICON_MAX_FA
import strongdmm.util.icons.ICON_MIN_FA
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

abstract class AppWindow(title: String) {
    companion object {
        private const val DEFAULT_WIDTH = 1280
        private const val DEFAULT_HEIGHT = 768

        var windowPtr: Long = 0
        var isRunning: Boolean = true

        // Those are used to track window size properties
        private val winWidth: IntArray = IntArray(1)
        private val winHeight: IntArray = IntArray(1)
        private val fbWidth: IntArray = IntArray(1)
        private val fbHeight: IntArray = IntArray(1)

        val windowWidth: Int
            get() = winWidth[0]
        val windowHeight: Int
            get() = winHeight[0]

        var defaultWindowCond: Int = ImGuiCond.Once
            private set

        private var resetWindows: Boolean = false

        // We will restore 'Once' condition after the first passed render cycle
        fun resetWindows() {
            resetWindows = true
        }
    }

    // For mouse tracking
    private val mousePosX: DoubleArray = DoubleArray(1)
    private val mousePosY: DoubleArray = DoubleArray(1)

    // Mouse cursors provided by GLFW
    private val mouseCursors: LongArray = LongArray(ImGuiMouseCursor.COUNT)

    private val sync: Sync = Sync()
    private val imGuiGl3: ImGuiImplGl3 = ImGuiImplGl3()

    init {
        initGlfw(title)
        initImGui()
    }

    fun start() {
        loop()
        destroyImGui()
        destroyGlfw()
    }

    // Method initializes GLFW window
    private fun initGlfw(title: String) {
        // Setup an error callback. The default implementation will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize  Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE) // the window will be maximized

        // Create the window
        windowPtr = glfwCreateWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT, title, MemoryUtil.NULL, MemoryUtil.NULL)

        if (windowPtr == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowPtr, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            // Center the window
            glfwSetWindowPos(windowPtr, (vidmode.width() - pWidth[0]) / 2, (vidmode.height() - pHeight[0]) / 2)
            loadWindowIcon(stack)
        }

        glfwMakeContextCurrent(windowPtr) // Make the OpenGL context current
        glfwSwapInterval(GLFW_TRUE) // Enable v-sync
        glfwShowWindow(windowPtr) // Make the window visible

        GL.createCapabilities()
    }

    // Here we will initialize ImGui stuff.
    private fun initImGui() {
        ImGui.createContext()
        ImGui.styleColorsDark()

        val io = ImGui.getIO()

        io.iniFilename = null
        io.backendFlags = ImGuiBackendFlags.HasMouseCursors

        // Keyboard mapping. ImGui will use those indices to peek into the io.KeysDown[] array.
        val keyMap = IntArray(ImGuiKey.COUNT)
        keyMap[ImGuiKey.Tab] = GLFW_KEY_TAB
        keyMap[ImGuiKey.LeftArrow] = GLFW_KEY_LEFT
        keyMap[ImGuiKey.RightArrow] = GLFW_KEY_RIGHT
        keyMap[ImGuiKey.UpArrow] = GLFW_KEY_UP
        keyMap[ImGuiKey.DownArrow] = GLFW_KEY_DOWN
        keyMap[ImGuiKey.PageUp] = GLFW_KEY_PAGE_UP
        keyMap[ImGuiKey.PageDown] = GLFW_KEY_PAGE_DOWN
        keyMap[ImGuiKey.Home] = GLFW_KEY_HOME
        keyMap[ImGuiKey.End] = GLFW_KEY_END
        keyMap[ImGuiKey.Insert] = GLFW_KEY_INSERT
        keyMap[ImGuiKey.Delete] = GLFW_KEY_DELETE
        keyMap[ImGuiKey.Backspace] = GLFW_KEY_BACKSPACE
        keyMap[ImGuiKey.Space] = GLFW_KEY_SPACE
        keyMap[ImGuiKey.Enter] = GLFW_KEY_ENTER
        keyMap[ImGuiKey.Escape] = GLFW_KEY_ESCAPE
        keyMap[ImGuiKey.KeyPadEnter] = GLFW_KEY_KP_ENTER
        keyMap[ImGuiKey.A] = GLFW_KEY_A
        keyMap[ImGuiKey.C] = GLFW_KEY_C
        keyMap[ImGuiKey.V] = GLFW_KEY_V
        keyMap[ImGuiKey.X] = GLFW_KEY_X
        keyMap[ImGuiKey.Y] = GLFW_KEY_Y
        keyMap[ImGuiKey.Z] = GLFW_KEY_Z
        io.setKeyMap(keyMap)

        // Mouse cursors mapping
        mouseCursors[ImGuiMouseCursor.Arrow] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR)
        mouseCursors[ImGuiMouseCursor.TextInput] = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeAll] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeNS] = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeEW] = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeNESW] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeNWSE] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR)
        mouseCursors[ImGuiMouseCursor.Hand] = glfwCreateStandardCursor(GLFW_HAND_CURSOR)
        mouseCursors[ImGuiMouseCursor.NotAllowed] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR)

        // Here goes GLFW callbacks to update user input stuff in ImGui
        glfwSetKeyCallback(windowPtr) { _, key: Int, _, action: Int, _ ->
            if (action == GLFW_PRESS) {
                io.setKeysDown(key, true)
            } else if (action == GLFW_RELEASE) {
                io.setKeysDown(key, false)
            }

            io.keyCtrl = io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL)
            io.keyShift = io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT)
            io.keyAlt = io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT)
            io.keySuper = io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER)
        }

        glfwSetCharCallback(windowPtr) { _, c: Int ->
            if (c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c)
            }
        }

        glfwSetMouseButtonCallback(windowPtr) { _, button: Int, action: Int, _ ->
            val mouseDown = BooleanArray(5)

            mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE
            mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE
            mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE
            mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE
            mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE

            io.setMouseDown(mouseDown)

            if (!io.wantCaptureMouse && mouseDown[1]) {
                ImGui.setWindowFocus(null)
            }
        }

        glfwSetScrollCallback(windowPtr) { _, xOffset: Double, yOffset: Double ->
            io.mouseWheelH = io.mouseWheelH + xOffset.toFloat()
            io.mouseWheel = io.mouseWheel + yOffset.toFloat()
        }

        io.setSetClipboardTextFn(object : ImStrConsumer() {
            override fun accept(s: String) {
                glfwSetClipboardString(windowPtr, s)
            }
        })

        io.setGetClipboardTextFn(object : ImStrSupplier() {
            override fun get(): String? {
                return glfwGetClipboardString(windowPtr).let { it ?: "" }
            }
        })

        // Fonts configuration
        val fontAtlas = io.fonts
        val fontConfig = ImFontConfig()

        javaClass.classLoader.getResourceAsStream("Ruda-Bold.ttf")!!.use {
            fontAtlas.addFontFromMemoryTTF(it.readAllBytes(), 15f, fontConfig, fontAtlas.glyphRangesCyrillic)
        }

        // Add Font Awesome icons
        javaClass.classLoader.getResourceAsStream("fa-solid-900.ttf")!!.use {
            fontConfig.mergeMode = true
            fontConfig.glyphMaxAdvanceX = 13f
            val iconRange = shortArrayOf(ICON_MIN_FA, ICON_MAX_FA)
            fontAtlas.addFontFromMemoryTTF(it.readAllBytes(), 13f, fontConfig, iconRange)
        }

        ImGuiFreeType.buildFontAtlas(fontAtlas, ImGuiFreeType.RasterizerFlags.LightHinting)
        fontConfig.destroy()

        // Custom Styling
        ImGui.getStyle().apply {
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

        imGuiGl3.init()
    }

    private fun loop() {
        var time = 0.0 // to track our frame delta value

        // Respect alpha channel
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Run the rendering loop until the user has attempted to close the window
        while (isRunning) { // Count frame delta value
            val currentTime = glfwGetTime()
            val deltaTime = if (time > 0) currentTime - time else (1f / 60f).toDouble()
            time = currentTime

            updateWindowProperties()
            updateImGuiIO(deltaTime.toFloat())
            updateMouseCursor()

            startFrame()
            appLoop()
            endFrame()

            // 60 fps lock
            sync.sync(60)
        }
    }

    private fun updateWindowProperties() {
        glfwGetWindowSize(windowPtr, winWidth, winHeight)
        glfwGetFramebufferSize(windowPtr, fbWidth, fbHeight)
        glfwGetCursorPos(windowPtr, mousePosX, mousePosY)
    }

    private fun updateImGuiIO(deltaTime: Float) {
        val io = ImGui.getIO()
        io.setDisplaySize(winWidth[0].toFloat(), winHeight[0].toFloat())
        io.setDisplayFramebufferScale(fbWidth[0].toFloat() / winWidth[0], fbHeight[0].toFloat() / winHeight[0])
        io.setMousePos(mousePosX[0].toFloat(), mousePosY[0].toFloat())
        io.deltaTime = deltaTime
    }

    private fun updateMouseCursor() {
        val imguiCursor = ImGui.getMouseCursor()
        glfwSetCursor(windowPtr, mouseCursors[imguiCursor])
        glfwSetInputMode(windowPtr, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
    }

    private fun startFrame() {
        glClearColor(.25f, .25f, .5f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)
        ImGui.newFrame()
    }

    private fun endFrame() {
        ImGui.render()

        imGuiGl3.render(ImGui.getDrawData())
        checkWindowsState()

        glfwSwapBuffers(windowPtr) // swap the color buffers
        glfwPollEvents()
    }

    private fun checkWindowsState() {
        defaultWindowCond = ImGuiCond.Once // reset windows condition
        if (resetWindows) {
            resetWindows = false
            defaultWindowCond = ImGuiCond.Always
        }
    }

    abstract fun appLoop()

    private fun loadWindowIcon(stack: MemoryStack) {
        val icon = ImageIO.read(AppWindow::class.java.classLoader.getResource("icon.png"))

        val iconBuffer = ByteArrayOutputStream().use {
            ImageIO.write(icon, "png", it)
            it.flush()

            val iconInByte = it.toByteArray()

            BufferUtils.createByteBuffer(iconInByte.size).apply {
                put(iconInByte)
                flip()
            }
        }

        val comp = stack.mallocInt(1)
        val w = stack.mallocInt(1)
        val h = stack.mallocInt(1)
        val imageBuffer = STBImage.stbi_load_from_memory(iconBuffer, w, h, comp, 4)

        if (imageBuffer != null) {
            val image = GLFWImage.malloc()
            val imagePtr = GLFWImage.malloc(1)

            image.set(icon.width, icon.height, imageBuffer)
            imagePtr.put(0, image)

            glfwSetWindowIcon(windowPtr, imagePtr)
            STBImage.stbi_image_free(imageBuffer)

            imagePtr.free()
            image.free()
        }
    }

    // If you want to clean a room after yourself - do it by yourself
    private fun destroyImGui() {
        imGuiGl3.dispose()
        ImGui.destroyContext()
    }

    private fun destroyGlfw() {
        for (mouseCursor in mouseCursors) {
            glfwDestroyCursor(mouseCursor)
        }
        Callbacks.glfwFreeCallbacks(windowPtr)
        glfwDestroyWindow(windowPtr)
        glfwTerminate()
        Objects.requireNonNull(glfwSetErrorCallback(null))!!.free()
    }
}
