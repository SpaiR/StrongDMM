package strongdmm.byond.dmi

import ar.com.hjg.pngj.PngReader
import ar.com.hjg.pngj.chunks.PngMetadata
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import strongdmm.byond.DEFAULT_DIR
import strongdmm.util.DEFAULT_ICON_SIZE
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO

object GlobalDmiHolder {
    lateinit var environmentRootPath: String

    private val dmiCache: MutableMap<String, Dmi?> = mutableMapOf()

    private val placeholderDmi: Dmi
    private val placeholderSprite: IconSprite

    private val WIDTH_HEIGHT_PATTERN: Regex = "(?:width\\s=\\s(\\d+))\n\t(?:height\\s=\\s(\\d+))".toRegex()
    private val STATE_PATTERN: Regex = "(?:state\\s=\\s\".*\"(?:\\n\\t.*)+)".toRegex()
    private val PARAM_PATTERN: Regex = "(\\w+)\\s=\\s(.+)".toRegex()

    init { // Initialize placeholder texture
        val placeholderImage = ImageIO.read(javaClass.classLoader.getResource("placeholder.png"))
        val placeholderTextureId = loadTexture(placeholderImage)
        val placeholderIconStates = mutableMapOf<String, IconState>()

        placeholderDmi = Dmi(32, 32, 1, 1, placeholderTextureId, placeholderIconStates)

        val placeholderIconSprite = IconSprite(placeholderDmi, 0)
        placeholderIconStates[""] = IconState(1, 1, mutableListOf(placeholderIconSprite))

        placeholderSprite = placeholderDmi.getIconState("")!!.getIconSprite()
    }

    fun resetEnvironment() {
        glBindTexture(GL_TEXTURE_2D, 0)

        dmiCache.values.filterNotNull().forEach { dmi ->
            glDeleteTextures(dmi.textureId)
        }

        dmiCache.clear()
    }

    fun getDmi(icon: String): Dmi? {
        if (icon.isEmpty()) {
            return null
        }

        if (dmiCache.containsKey(icon)) {
            return dmiCache.getValue(icon)
        }

        val dmiFile = File(environmentRootPath + File.separator + icon)

        if (!dmiFile.exists() || !dmiFile.isFile) {
            dmiCache[icon] = null
            return null
        }

        val imageMeta: Metadata

        // Read dmi metadata
        with(PngReader(dmiFile)) {
            try {
                imageMeta = metadata.extractMetadata()
            } catch (e: Exception) {
                dmiCache[icon] = null
                return null
            } finally {
                close()
            }
        }

        var width = 0
        var height = 0
        var texture = -1

        // Read an image itself
        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val comp = stack.mallocInt(1)

            val imageBuffer = STBImage.stbi_load(dmiFile.absolutePath, w, h, comp, 4)

            if (imageBuffer != null) {
                width = w.get()
                height = h.get()
                texture = createTexture(width, height, imageBuffer)
                STBImage.stbi_image_free(imageBuffer)
            }
        }

        // Unable to load provided icon
        if (texture == -1) {
            dmiCache[icon] = null
            return null
        }

        val dmiCols = width / imageMeta.spriteWidth
        val dmiRows = height / imageMeta.spriteHeight

        val iconStates = mutableMapOf<String, IconState>()
        val dmi = Dmi(imageMeta.spriteWidth, imageMeta.spriteHeight, dmiRows, dmiCols, texture, iconStates)

        var spriteIndex = 0

        // Save all icon states
        imageMeta.states.forEach { state ->
            val iconSprites = mutableListOf<IconSprite>()

            for (i in 0 until state.dirs * state.frames) {
                iconSprites.add(IconSprite(dmi, spriteIndex++))
            }

            with(state) {
                iconStates[name] = IconState(dirs, frames, iconSprites)
            }
        }

        dmiCache[icon] = dmi

        return dmi
    }

    fun getIconState(icon: String, iconState: String): IconState? {
        return getDmi(icon)?.getIconState(iconState)
    }

    fun getIconSpriteOrPlaceholder(icon: String, iconState: String, dir: Int = DEFAULT_DIR): IconSprite {
        return getIconState(icon, iconState)?.getIconSprite(dir) ?: placeholderSprite
    }

    private fun PngMetadata.extractMetadata(): Metadata {
        val textMeta = getTxtForKey("Description")

        val widthHeight = WIDTH_HEIGHT_PATTERN.toPattern().matcher(textMeta).apply { find() }
        val width = widthHeight.group(1)?.toInt() ?: DEFAULT_ICON_SIZE
        val height = widthHeight.group(2)?.toInt() ?: DEFAULT_ICON_SIZE

        val states = mutableListOf<MetadataState>()

        with(STATE_PATTERN.toPattern().matcher(textMeta)) {
            while (find()) {
                val stateParam = PARAM_PATTERN.toPattern().matcher(group())

                var name = ""
                var dirs = 1
                var frames = 1

                while (stateParam.find()) {
                    val paramName = stateParam.group(1)
                    val paramValue = stateParam.group(2)

                    when (paramName) {
                        "state" -> name = paramValue.substring(1, paramValue.length - 1)
                        "dirs" -> dirs = paramValue.toInt()
                        "frames" -> frames = paramValue.toInt()
                    }
                }

                states.add(MetadataState(name, dirs, frames))
            }
        }

        return Metadata(width, height, states)
    }

    private fun loadTexture(img: BufferedImage): Int {
        val pixels = img.getRGB(0, 0, img.width, img.height, null, 0, img.width)

        val buffer = BufferUtils.createByteBuffer(img.width * img.height * 4).apply {
            for (y in 0 until img.height) {
                for (x in 0 until img.width) {
                    val pixel = pixels[y * img.width + x]
                    put((pixel shr 16 and 0xFF).toByte()) // Red
                    put((pixel shr 8 and 0xFF).toByte()) // Green
                    put((pixel and 0xFF).toByte()) // Blue
                    put((pixel shr 24 and 0xFF).toByte()) // Alpha
                }
            }

            flip()
        }

        return createTexture(img.width, img.height, buffer)
    }

    private fun createTexture(width: Int, height: Int, image: ByteBuffer): Int {
        val textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureId)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE)

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image)

        return textureId
    }

    private class Metadata(val spriteWidth: Int, val spriteHeight: Int, val states: List<MetadataState>)
    private class MetadataState(val name: String, val dirs: Int, val frames: Int)
}
