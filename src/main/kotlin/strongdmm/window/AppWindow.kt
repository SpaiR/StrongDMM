package strongdmm.window

import imgui.ImGui
import imgui.callbacks.ImStrConsumer
import imgui.callbacks.ImStrSupplier
import imgui.enums.ImGuiBackendFlags
import imgui.enums.ImGuiConfigFlags
import imgui.enums.ImGuiKey
import imgui.enums.ImGuiMouseCursor
import imgui.gl3.ImGuiImplGl3
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.io.ByteArrayOutputStream
import java.util.Objects
import javax.imageio.ImageIO

// Modified https://github.com/SpaiR/imgui-java/blob/v1.74-0.3/imgui-lwjgl3/src/test/java/ImGuiGlfwExample.java
abstract class AppWindow(title: String) {
    companion object {
        var window: Long = 0
        private const val DEFAULT_WIDTH = 1280
        private const val DEFAULT_HEIGHT = 768
    }

    // Those are used to track window size properties
    private val winWidth: IntArray = IntArray(1)
    private val winHeight: IntArray = IntArray(1)
    private val fbWidth: IntArray = IntArray(1)
    private val fbHeight: IntArray = IntArray(1)

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

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE) // the window will be resizable
        GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE) // the window will be maximized

        // Create the window
        window = GLFW.glfwCreateWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT, title, MemoryUtil.NULL, MemoryUtil.NULL)

        if (window == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode: GLFWVidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!

            // Center the window
            GLFW.glfwSetWindowPos(window, (vidmode.width() - pWidth[0]) / 2, (vidmode.height() - pHeight[0]) / 2)
            loadWindowIcon(stack)
        }

        GLFW.glfwMakeContextCurrent(window) // Make the OpenGL context current
        GLFW.glfwSwapInterval(GLFW.GLFW_TRUE) // Enable v-sync
        GLFW.glfwShowWindow(window) // Make the window visible

        GL.createCapabilities()
    }

    // Here we will initialize ImGui stuff.
    private fun initImGui() {
        ImGui.createContext()
        ImGui.styleColorsDark()

        val io = ImGui.getIO()

        io.iniFilename = null
        io.configFlags = ImGuiConfigFlags.NavEnableKeyboard
        io.backendFlags = ImGuiBackendFlags.HasMouseCursors

        // Keyboard mapping. ImGui will use those indices to peek into the io.KeysDown[] array.
        val keyMap = IntArray(ImGuiKey.COUNT)
        keyMap[ImGuiKey.Tab] = GLFW.GLFW_KEY_TAB
        keyMap[ImGuiKey.LeftArrow] = GLFW.GLFW_KEY_LEFT
        keyMap[ImGuiKey.RightArrow] = GLFW.GLFW_KEY_RIGHT
        keyMap[ImGuiKey.UpArrow] = GLFW.GLFW_KEY_UP
        keyMap[ImGuiKey.DownArrow] = GLFW.GLFW_KEY_DOWN
        keyMap[ImGuiKey.PageUp] = GLFW.GLFW_KEY_PAGE_UP
        keyMap[ImGuiKey.PageDown] = GLFW.GLFW_KEY_PAGE_DOWN
        keyMap[ImGuiKey.Home] = GLFW.GLFW_KEY_HOME
        keyMap[ImGuiKey.End] = GLFW.GLFW_KEY_END
        keyMap[ImGuiKey.Insert] = GLFW.GLFW_KEY_INSERT
        keyMap[ImGuiKey.Delete] = GLFW.GLFW_KEY_DELETE
        keyMap[ImGuiKey.Backspace] = GLFW.GLFW_KEY_BACKSPACE
        keyMap[ImGuiKey.Space] = GLFW.GLFW_KEY_SPACE
        keyMap[ImGuiKey.Enter] = GLFW.GLFW_KEY_ENTER
        keyMap[ImGuiKey.Escape] = GLFW.GLFW_KEY_ESCAPE
        keyMap[ImGuiKey.KeyPadEnter] = GLFW.GLFW_KEY_KP_ENTER
        keyMap[ImGuiKey.A] = GLFW.GLFW_KEY_A
        keyMap[ImGuiKey.C] = GLFW.GLFW_KEY_C
        keyMap[ImGuiKey.V] = GLFW.GLFW_KEY_V
        keyMap[ImGuiKey.X] = GLFW.GLFW_KEY_X
        keyMap[ImGuiKey.Y] = GLFW.GLFW_KEY_Y
        keyMap[ImGuiKey.Z] = GLFW.GLFW_KEY_Z
        io.setKeyMap(keyMap)

        // Mouse cursors mapping
        mouseCursors[ImGuiMouseCursor.Arrow] = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
        mouseCursors[ImGuiMouseCursor.TextInput] = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeAll] = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeNS] = GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeEW] = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeNESW] = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
        mouseCursors[ImGuiMouseCursor.ResizeNWSE] = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
        mouseCursors[ImGuiMouseCursor.Hand] = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR)

        // Here goes GLFW callbacks to update user input stuff in ImGui
        GLFW.glfwSetKeyCallback(window) { _, key: Int, _, action: Int, _ ->
            if (action == GLFW.GLFW_PRESS) {
                io.setKeysDown(key, true)
            } else if (action == GLFW.GLFW_RELEASE) {
                io.setKeysDown(key, false)
            }

            io.keyCtrl = io.getKeysDown(GLFW.GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW.GLFW_KEY_RIGHT_CONTROL)
            io.keyShift = io.getKeysDown(GLFW.GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW.GLFW_KEY_RIGHT_SHIFT)
            io.keyAlt = io.getKeysDown(GLFW.GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW.GLFW_KEY_RIGHT_ALT)
            io.keySuper = io.getKeysDown(GLFW.GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW.GLFW_KEY_RIGHT_SUPER)
        }

        GLFW.glfwSetCharCallback(window) { w: Long, c: Int ->
            if (c != GLFW.GLFW_KEY_DELETE) {
                io.addInputCharacter(c)
            }
        }

        GLFW.glfwSetMouseButtonCallback(window) { _, button: Int, action: Int, _ ->
            val mouseDown = BooleanArray(5)

            mouseDown[0] = button == GLFW.GLFW_MOUSE_BUTTON_1 && action != GLFW.GLFW_RELEASE
            mouseDown[1] = button == GLFW.GLFW_MOUSE_BUTTON_2 && action != GLFW.GLFW_RELEASE
            mouseDown[2] = button == GLFW.GLFW_MOUSE_BUTTON_3 && action != GLFW.GLFW_RELEASE
            mouseDown[3] = button == GLFW.GLFW_MOUSE_BUTTON_4 && action != GLFW.GLFW_RELEASE
            mouseDown[4] = button == GLFW.GLFW_MOUSE_BUTTON_5 && action != GLFW.GLFW_RELEASE

            io.setMouseDown(mouseDown)

            if (!io.wantCaptureMouse && mouseDown[1]) {
                ImGui.setWindowFocus(null)
            }
        }

        GLFW.glfwSetScrollCallback(window) { _, xOffset: Double, yOffset: Double ->
            io.mouseWheelH = io.mouseWheelH + xOffset.toFloat()
            io.mouseWheel = io.mouseWheel + yOffset.toFloat()
        }

        io.setSetClipboardTextFn(object : ImStrConsumer() {
            override fun accept(s: String) {
                GLFW.glfwSetClipboardString(window, s)
            }
        })

        io.setGetClipboardTextFn(object : ImStrSupplier() {
            override fun get(): String? {
                return GLFW.glfwGetClipboardString(window)
            }
        })

        // Initialize renderer itself
        imGuiGl3.init()
    }

    private fun loop() {
        var time = 0.0 // to track our frame delta value

        // Set the clear color
        GL11.glClearColor(.25f, .25f, .5f, 1f)

        // Respect alpha channel
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        // Run the rendering loop until the user has attempted to close the window
        while (!GLFW.glfwWindowShouldClose(window)) { // Count frame delta value
            val currentTime = GLFW.glfwGetTime()
            val deltaTime = if (time > 0) currentTime - time else (1f / 60f).toDouble()
            time = currentTime

            // Get window size properties and mouse position
            GLFW.glfwGetWindowSize(window, winWidth, winHeight)
            GLFW.glfwGetFramebufferSize(window, fbWidth, fbHeight)
            GLFW.glfwGetCursorPos(window, mousePosX, mousePosY)

            val io = ImGui.getIO()
            io.setDisplaySize(winWidth[0].toFloat(), winHeight[0].toFloat())
            io.setDisplayFramebufferScale(fbWidth[0].toFloat() / winWidth[0], fbHeight[0].toFloat() / winHeight[0])
            io.setMousePos(mousePosX[0].toFloat(), mousePosY[0].toFloat())
            io.deltaTime = deltaTime.toFloat()

            // Update mouse cursor
            val imguiCursor = ImGui.getMouseCursor()
            GLFW.glfwSetCursor(window, mouseCursors[imguiCursor])
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL)

            // Initialize OpenGL context to render a canvas
            GL11.glViewport(0, 0, fbWidth[0], fbHeight[0])
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glLoadIdentity()
            GL11.glOrtho(0.0, fbWidth[0].toDouble(), 0.0, fbHeight[0].toDouble(), -1.0, 1.0)
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glLoadIdentity()

            // Clear the framebuffer
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)

            ImGui.newFrame()
            appLoop(fbWidth[0], fbHeight[0])
            ImGui.render()

            imGuiGl3.render(ImGui.getDrawData())

            GLFW.glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be invoked during this call.
            GLFW.glfwPollEvents()

            // 60 fps lock
            sync.sync(60)
        }
    }

    abstract fun appLoop(windowWidth: Int, windowHeight: Int)

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

        imageBuffer?.let {
            val image = GLFWImage.malloc()
            val imagebf = GLFWImage.malloc(1)

            image.set(icon.width, icon.height, it)
            imagebf.put(0, image)

            GLFW.glfwSetWindowIcon(window, imagebf)
        }
    }

    // If you want to clean a room after yourself - do it by yourself
    private fun destroyImGui() {
        imGuiGl3.dispose()
        ImGui.destroyContext()
    }

    private fun destroyGlfw() {
        for (mouseCursor in mouseCursors) {
            GLFW.glfwDestroyCursor(mouseCursor)
        }
        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)
        GLFW.glfwTerminate()
        Objects.requireNonNull(GLFW.glfwSetErrorCallback(null))!!.free()
    }
}
