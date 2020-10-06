package strongdmm.window

import imgui.ImFontAtlas
import imgui.ImFontConfig
import imgui.ImGui
import imgui.ImGuiFreeType
import imgui.flag.ImGuiCond
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
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

    private val sync = Sync()
    private val imGuiGlfw = ImGuiImplGlfw()
    private val imGuiGl3 = ImGuiImplGl3()

    private lateinit var fontData: ByteArray
    private lateinit var iconData: ByteArray

    init {
        ImGui.createContext()

        setupGlfw(title)
        imGuiGlfw.init(Window.ptr, true)

        setupImGui()
        imGuiGl3.init()
    }

    fun start() {
        loop()

        imGuiGl3.dispose()
        imGuiGlfw.dispose()

        ImGui.destroyContext()

        disposeWindow()
    }

    // Method initializes GLFW window
    private fun setupGlfw(title: String) {
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
    private fun setupImGui() {
        ImGui.getIO().iniFilename = null

        // Read font
        javaClass.classLoader.getResourceAsStream("fonts/Ruda-Bold.ttf")!!.use {
            fontData = it.readAllBytes()
        }

        // Read Font Awesome icons
        javaClass.classLoader.getResourceAsStream("fonts/font-awesome-solid-900.ttf")!!.use {
            iconData = it.readAllBytes()
        }

        configureFonts()
    }

    private fun loop() {
        // Respect alpha channel
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Run the rendering loop until the user has attempted to close the window
        while (Window.isRunning) { // Count frame delta value
            glfwGetWindowSize(Window.ptr, Window._width, Window._height)

            startFrame()
            appLoop()
            endFrame()

            // 60 fps lock
            sync.sync(60)
        }
    }

    private fun startFrame() {
        imGuiGlfw.newFrame()
        ImGui.newFrame()

        glClearColor(.25f, .25f, .5f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)
    }

    private fun endFrame() {
        ImGui.render()

        imGuiGl3.renderDrawData(ImGui.getDrawData())

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

        createDefaultFont(fontAtlas, fontConfig)
        createHeaderFont(fontAtlas, fontConfig)

        ImGuiFreeType.buildFontAtlas(fontAtlas, ImGuiFreeType.RasterizerFlags.LightHinting)
        fontConfig.destroy()

        ImGui.getIO().setFontDefault(Window.defaultFont)
    }

    private fun createDefaultFont(fontAtlas: ImFontAtlas, fontConfig: ImFontConfig) {
        Window.defaultFont = fontAtlas.addFontFromMemoryTTF(fontData, 15 * Window.pointSize, fontConfig, fontAtlas.glyphRangesCyrillic)

        // Add Font Awesome icons
        val iconSize = 13 * Window.pointSize

        fontConfig.mergeMode = true
        fontConfig.glyphMaxAdvanceX = iconSize

        fontAtlas.addFontFromMemoryTTF(iconData, iconSize, fontConfig, shortArrayOf(ICON_MIN_FA, ICON_MAX_FA))

        fontConfig.mergeMode = false
    }

    private fun createHeaderFont(fontAtlas: ImFontAtlas, fontConfig: ImFontConfig) {
        Window.headerFont = fontAtlas.addFontFromMemoryTTF(fontData, 18 * Window.pointSize, fontConfig, fontAtlas.glyphRangesCyrillic)

        // Add Font Awesome icons
        val headerIconSize = 16 * Window.pointSize

        fontConfig.mergeMode = true
        fontConfig.glyphMaxAdvanceX = headerIconSize

        fontAtlas.addFontFromMemoryTTF(iconData, headerIconSize, fontConfig, shortArrayOf(ICON_MIN_FA, ICON_MAX_FA))

        fontConfig.mergeMode = false
    }

    private fun loadWindowIcon(stack: MemoryStack) {
        val icon = ImageIO.read(AppWindow::class.java.classLoader.getResource("img/icon.png"))

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

    private fun disposeWindow() {
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
