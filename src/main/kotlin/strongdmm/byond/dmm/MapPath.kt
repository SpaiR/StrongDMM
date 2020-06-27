package strongdmm.byond.dmm

import java.io.File

data class MapPath(
    val readable: String = "",
    val absolute: String = ""
) {
    val fileName: String
        get() = readable.substringAfterLast(File.separatorChar)
}
