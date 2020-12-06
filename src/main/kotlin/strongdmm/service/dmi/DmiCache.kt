package strongdmm.service.dmi

import strongdmm.byond.DEFAULT_DIR
import strongdmm.byond.dmi.Dmi
import strongdmm.byond.dmi.IconSprite
import strongdmm.byond.dmi.IconState
import java.io.File

class DmiCache(
    private val dmiLoader: DmiLoader
) {
    private lateinit var environmentRootPath: String
    private val dmiCache: MutableMap<String, Dmi?> = mutableMapOf()

    fun init(environmentRootPath: String) {
        this.environmentRootPath = environmentRootPath
    }

    fun reset() {
        dmiCache.values.filterNotNull().forEach(dmiLoader::free)
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

        val dmi = dmiLoader.load(dmiFile)
        dmiCache[icon] = dmi
        return dmi
    }

    fun getIconState(icon: String, iconState: String): IconState? {
        return getDmi(icon)?.getIconState(iconState)
    }

    fun getIconSpriteOrPlaceholder(icon: String, iconState: String, dir: Int = DEFAULT_DIR): IconSprite {
        return getIconState(icon, iconState)?.getIconSprite(dir) ?: dmiLoader.placeholderSprite
    }
}
