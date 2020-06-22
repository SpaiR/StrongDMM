package strongdmm.window

import imgui.ImFontConfig
import imgui.ImGui
import imgui.ImGuiFreeType
import imgui.callback.ImStrConsumer
import imgui.callback.ImStrSupplier
import imgui.flag.*
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
import kotlin.math.max
import kotlin.math.min

abstract class AppWindow(title: String) {
    companion object {
        // We will restore 'Once' condition after the first passed render cycle
        fun resetWindows() {
            Window._resetWindows = true
        }

        fun toggleFullscreen() {
            Window._toggleFullscreen = true
        }
    }

    // For mouse tracking
    private val mousePosX: DoubleArray = DoubleArray(1)
    private val mousePosY: DoubleArray = DoubleArray(1)

    // Mouse cursors provided by GLFW
    private val mouseCursors: LongArray = LongArray(ImGuiMouseCursor.COUNT)

    private val sync: Sync = Sync()
    private val imGuiGl3: ImGuiImplGl3 = ImGuiImplGl3()

    private lateinit var fontData: ByteArray
    private lateinit var iconData: ByteArray

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

        // Create the window
        Window.ptr = glfwCreateWindow(1280, 768, title, MemoryUtil.NULL, MemoryUtil.NULL)

        if (Window.ptr == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(Window.ptr, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            // Center the window
            glfwSetWindowPos(Window.ptr, (vidmode.width() - pWidth[0]) / 2, (vidmode.height() - pHeight[0]) / 2)
            loadWindowIcon(stack)
        }

        glfwMakeContextCurrent(Window.ptr) // Make the OpenGL context current
        glfwSwapInterval(GLFW_TRUE) // Enable v-sync
        glfwShowWindow(Window.ptr) // Make the window visible
        glfwMaximizeWindow(Window.ptr)

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
        glfwSetKeyCallback(Window.ptr) { _, key: Int, _, action: Int, _ ->
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

        glfwSetCharCallback(Window.ptr) { _, c: Int ->
            if (c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c)
            }
        }

        glfwSetMouseButtonCallback(Window.ptr) { _, button: Int, action: Int, _ ->
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

        glfwSetScrollCallback(Window.ptr) { _, xOffset: Double, yOffset: Double ->
            io.mouseWheelH = io.mouseWheelH + xOffset.toFloat()
            io.mouseWheel = io.mouseWheel + yOffset.toFloat()
        }

        io.setSetClipboardTextFn(object : ImStrConsumer() {
            override fun accept(s: String) {
                glfwSetClipboardString(Window.ptr, s)
            }
        })

        io.setGetClipboardTextFn(object : ImStrSupplier() {
            override fun get(): String? {
                return glfwGetClipboardString(Window.ptr).let { it ?: "" }
            }
        })

        // Fonts configuration

        // Read font
        javaClass.classLoader.getResourceAsStream("Ruda-Bold.ttf")!!.use {
            fontData = it.readAllBytes()
        }

        // Read Font Awesome icons
        javaClass.classLoader.getResourceAsStream("fa-solid-900.ttf")!!.use {
            iconData = it.readAllBytes()
        }

        configureFonts()

        imGuiGl3.init()
    }

    private fun loop() {
        var time = 0.0 // to track our frame delta value

        // Respect alpha channel
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Run the rendering loop until the user has attempted to close the window
        while (Window.isRunning) { // Count frame delta value
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
        glfwGetWindowSize(Window.ptr, Window._width, Window._height)
        glfwGetFramebufferSize(Window.ptr, Window._fbWidth, Window._fbHeight)
        glfwGetCursorPos(Window.ptr, mousePosX, mousePosY)
    }

    private fun updateImGuiIO(deltaTime: Float) {
        val io = ImGui.getIO()

        val scaleX = Window._fbWidth[0].toFloat() / Window._width[0]
        val scaleY = Window._fbHeight[0].toFloat() / Window._height[0]

        io.setDisplaySize(Window._fbWidth[0].toFloat(), Window._fbHeight[0].toFloat())
        io.setDisplayFramebufferScale(scaleX, scaleY)
        io.setMousePos(mousePosX[0].toFloat() * scaleX, mousePosY[0].toFloat() * scaleY)

        io.deltaTime = deltaTime
    }

    private fun updateMouseCursor() {
        val imguiCursor = ImGui.getMouseCursor()
        glfwSetCursor(Window.ptr, mouseCursors[imguiCursor])
        glfwSetInputMode(Window.ptr, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
    }

    private fun startFrame() {
        glClearColor(.25f, .25f, .5f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)
        ImGui.newFrame()
    }

    private fun endFrame() {
        ImGui.render()

        imGuiGl3.render(ImGui.getDrawData())

        glfwSwapBuffers(Window.ptr) // swap the color buffers
        glfwPollEvents()

        checkWindowStateChanged()
    }

    private fun checkWindowStateChanged() {
        Window.windowCond = ImGuiCond.Once // reset windows condition

        if (Window._resetWindows) {
            Window._resetWindows = false
            Window.windowCond = ImGuiCond.Always
        }

        if (Window._toggleFullscreen) {
            Window._toggleFullscreen = false
            toggleFullscreen()
        }

        if (Window.pointSize != Window.newPointSize) {
            Window.pointSize = Window.newPointSize

            configureFonts()
            imGuiGl3.updateFontsTexture()

            Window.windowCond = ImGuiCond.Always
        }
    }

    abstract fun appLoop()

    private fun configureFonts() {
        val fontAtlas = ImGui.getIO().fonts
        val fontConfig = ImFontConfig()

        fontAtlas.clear()

        // Add default font
        fontAtlas.addFontFromMemoryTTF(fontData, 15f * Window.pointSize, fontConfig, fontAtlas.glyphRangesCyrillic)

        // Add Font Awesome icons
        val iconSize = 13f * Window.pointSize

        fontConfig.mergeMode = true
        fontConfig.glyphMaxAdvanceX = iconSize

        fontAtlas.addFontFromMemoryTTF(iconData, iconSize, fontConfig, shortArrayOf(ICON_MIN_FA, ICON_MAX_FA))

        ImGuiFreeType.buildFontAtlas(fontAtlas, ImGuiFreeType.RasterizerFlags.LightHinting)
        fontConfig.destroy()
    }

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

            glfwSetWindowIcon(Window.ptr, imagePtr)
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
        Callbacks.glfwFreeCallbacks(Window.ptr)
        glfwDestroyWindow(Window.ptr)
        glfwTerminate()
        Objects.requireNonNull(glfwSetErrorCallback(null))!!.free()
    }

    private fun toggleFullscreen() {
        Window.isFullscreen = !Window.isFullscreen

        val currentMonitor = getCurrentMonitor()
        val mode = glfwGetVideoMode(currentMonitor)!!

        glfwSetWindowMonitor(Window.ptr, if (Window.isFullscreen) currentMonitor else MemoryUtil.NULL, 0, 0, mode.width(), mode.height(), GLFW_DONT_CARE)

        if (!Window.isFullscreen) {
            glfwMaximizeWindow(Window.ptr)
        }
    }

    private fun getCurrentMonitor(): Long {
        val wx = IntArray(1)
        val wy = IntArray(1)
        val ww = IntArray(1)
        val wh = IntArray(1)

        val mx = IntArray(1)
        val my = IntArray(1)

        glfwGetWindowPos(Window.ptr, wx, wy)
        glfwGetWindowSize(Window.ptr, ww, wh)

        val monitors = glfwGetMonitors()!!

        var bestOverlap = 0
        var bestMonitor = 0L

        for (i in 0 until monitors.limit()) {
            val monitor = monitors[i]
            val mode = glfwGetVideoMode(monitor)!!

            glfwGetMonitorPos(monitor, mx, my)

            val mw = mode.width()
            val mh = mode.height()

            val overlap = max(0, min(wx[0] + ww[0], mx[0] + mw) - max(wx[0], mx[0])) *
                max(0, min(wy[0] + wh[0], my[0] + mh) - max(wy[0], my[0]))

            if (bestOverlap < overlap) {
                bestOverlap = overlap
                bestMonitor = monitor
            }
        }

        return bestMonitor
    }
}
