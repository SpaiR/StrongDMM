package strongdmm.window

import imgui.ImFontConfig
import imgui.ImGui
import imgui.callbacks.ImStrConsumer
import imgui.callbacks.ImStrSupplier
import imgui.enums.ImGuiBackendFlags
import imgui.enums.ImGuiConfigFlags
import imgui.enums.ImGuiKey
import imgui.enums.ImGuiMouseCursor
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
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

abstract class AppWindow(title: String) {
    companion object {
        private const val DEFAULT_WIDTH = 1280
        private const val DEFAULT_HEIGHT = 768

        // Those are used to track window size properties
        private val winWidth: IntArray = IntArray(1)
        private val winHeight: IntArray = IntArray(1)
        private val fbWidth: IntArray = IntArray(1)
        private val fbHeight: IntArray = IntArray(1)

        var window: Long = 0

        val windowWidth: Int
            get() = winWidth[0]
        val windowHeight: Int
            get() = winHeight[0]
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
        window = glfwCreateWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT, title, MemoryUtil.NULL, MemoryUtil.NULL)

        if (window == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            // Center the window
            glfwSetWindowPos(window, (vidmode.width() - pWidth[0]) / 2, (vidmode.height() - pHeight[0]) / 2)
            loadWindowIcon(stack)
        }

        glfwMakeContextCurrent(window) // Make the OpenGL context current
        glfwSwapInterval(GLFW_TRUE) // Enable v-sync
        glfwShowWindow(window) // Make the window visible

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
        glfwSetKeyCallback(window) { _, key: Int, _, action: Int, _ ->
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

        glfwSetCharCallback(window) { _, c: Int ->
            if (c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c)
            }
        }

        glfwSetMouseButtonCallback(window) { _, button: Int, action: Int, _ ->
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

        glfwSetScrollCallback(window) { _, xOffset: Double, yOffset: Double ->
            io.mouseWheelH = io.mouseWheelH + xOffset.toFloat()
            io.mouseWheel = io.mouseWheel + yOffset.toFloat()
        }

        io.setSetClipboardTextFn(object : ImStrConsumer() {
            override fun accept(s: String) {
                glfwSetClipboardString(window, s)
            }
        })

        io.setGetClipboardTextFn(object : ImStrSupplier() {
            override fun get(): String? {
                return glfwGetClipboardString(window)
            }
        })

        // Fonts configuration
        val fontAtlas = io.fonts
        fontAtlas.addFontDefault() // ProggyClean.ttf, 13px

        val fontConfig = ImFontConfig()

        fontConfig.mergeMode = true
        fontConfig.pixelSnapH = true

        javaClass.classLoader.getResourceAsStream("basis33.ttf")!!.use {
            fontAtlas.addFontFromMemoryTTF(it.readAllBytes(), 16f, fontConfig, fontAtlas.glyphRangesCyrillic)
        }

        fontConfig.destroy()

        // Initialize renderer itself
        imGuiGl3.init()
    }

    private fun loop() {
        var time = 0.0 // to track our frame delta value

        // Set the clear color
        glClearColor(.25f, .25f, .5f, 1f)

        // Respect alpha channel
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Run the rendering loop until the user has attempted to close the window
        while (!glfwWindowShouldClose(window)) { // Count frame delta value
            val currentTime = glfwGetTime()
            val deltaTime = if (time > 0) currentTime - time else (1f / 60f).toDouble()
            time = currentTime

            // Get window size properties and mouse position
            glfwGetWindowSize(window, winWidth, winHeight)
            glfwGetFramebufferSize(window, fbWidth, fbHeight)
            glfwGetCursorPos(window, mousePosX, mousePosY)

            val io = ImGui.getIO()
            io.setDisplaySize(winWidth[0].toFloat(), winHeight[0].toFloat())
            io.setDisplayFramebufferScale(fbWidth[0].toFloat() / winWidth[0], fbHeight[0].toFloat() / winHeight[0])
            io.setMousePos(mousePosX[0].toFloat(), mousePosY[0].toFloat())
            io.deltaTime = deltaTime.toFloat()

            // Update mouse cursor
            val imguiCursor = ImGui.getMouseCursor()
            glfwSetCursor(window, mouseCursors[imguiCursor])
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)

            // Clear the framebuffer
            glClear(GL_COLOR_BUFFER_BIT)

            ImGui.newFrame()
            appLoop()
            ImGui.render()

            imGuiGl3.render(ImGui.getDrawData())

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be invoked during this call.
            glfwPollEvents()

            // 60 fps lock
            sync.sync(60)
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

            glfwSetWindowIcon(window, imagePtr)
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
        Callbacks.glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        glfwTerminate()
        Objects.requireNonNull(glfwSetErrorCallback(null))!!.free()
    }
}
