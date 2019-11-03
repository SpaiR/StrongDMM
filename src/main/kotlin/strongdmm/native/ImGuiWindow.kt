package strongdmm.native

import imgui.ConfigFlag
import imgui.ImGui
import imgui.imgui.Context
import imgui.impl.ImplGL3
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.stb.STBImage.stbi_load
import org.lwjgl.system.MemoryStack
import uno.glfw.VSync
import uno.glfw.glfw
import java.nio.ByteBuffer
import uno.glfw.GlfwWindow as UnoGlfwWindow

abstract class ImGuiWindow {
    private val sync = Sync()

    private val window: UnoGlfwWindow
    private val ctx: Context

    private val glfwWindow: GlfwWindow
    private val gl: ImplGL3

    init {
        imgui.DEBUG = false

        // Setup window
        glfw {
            errorCallback = { error, description -> println("Glfw Error $error: $description") }
            init()
            windowHint {
                visible = false
            }
        }

        // Create window with graphics context
        window = UnoGlfwWindow(1280, 768, "StrongDMM")
        window.makeContextCurrent()
        window.maximize()
        window.show(true)

        glfw.swapInterval = VSync.OFF

        // Center Window on Monitor
        val windowSize = window.size
        val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!
        glfwSetWindowPos(window.handle.L, (vidmode.width() - windowSize.x) / 2, (vidmode.height() - windowSize.y) / 2)

        loadWindowIcon(window.handle.L)

        // Initialize OpenGL loader
        GL.createCapabilities()

        // Setup Dear ImGui context
        ctx = Context()
        ImGui.io.configFlags = ImGui.io.configFlags or ConfigFlag.NavEnableKeyboard.i // Enable Keyboard Controls
        ImGui.io.iniFilename = null

        // Setup Dear ImGui style
        ImGui.styleColorsDark()

        // Setup Platform/Renderer bindings
        glfwWindow = GlfwWindow(window)
        gl = ImplGL3()

        // Respect alpha channel
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun start() {
        window.loop(::mainLoop)
        gl.shutdown()
        glfwWindow.shutdown()
        ctx.destroy()
    }

    abstract fun guiLoop(windowWidth: Int, windowHeight: Int) // Used to draw UI elements
    abstract fun controllerLoop(windowWidth: Int, windowHeight: Int) // For controllers etc.

    @Suppress("UNUSED_PARAMETER")
    private fun mainLoop(s: MemoryStack) {
        // Start the Dear ImGui frame
        gl.newFrame()
        glfwWindow.newFrame()

        val (width, height) = window.framebufferSize

        GL11.glViewport(0, 0, width, height)
        GL11.glClearColor(.25f, .25f, .5f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)

        // Initialize OpenGL context to render a canvas
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glLoadIdentity()
        GL11.glOrtho(0.0, width.toDouble(), 0.0, height.toDouble(), -1.0, 1.0)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glLoadIdentity()

        // Start the Dear ImGui frame
        ImGui.newFrame()
        guiLoop(width, height)
        controllerLoop(width, height)
        ImGui.render()

        gl.renderDrawData(ImGui.drawData!!)

        // 60 fps lock
        sync.sync(60)
    }

    private fun loadWindowIcon(window: Long) {
        var imageBuffer: ByteBuffer? = null
        var width = 0
        var heigh = 0

        MemoryStack.stackPush().use { stack ->
            val comp = stack.mallocInt(1)
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)

            imageBuffer = stbi_load("icon.png", w, h, comp, 4)
            width = w.get()
            heigh = h.get()
        }

        imageBuffer?.let {
            val image = GLFWImage.malloc()
            val imagebf = GLFWImage.malloc(1)

            image.set(width, heigh, it)
            imagebf.put(0, image)

            glfwSetWindowIcon(window, imagebf)
        }
    }
}
