package strongdmm.native

import kool.use
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog

object NfdUtil {
    fun selectFile(filter: String, defaultPath: String = System.getProperty("user.home")): String? {
        var path: String? = null

        MemoryUtil.memAllocPointer(1).use { outPath ->
            if (NativeFileDialog.NFD_OpenDialog(filter, defaultPath, outPath) == NativeFileDialog.NFD_OKAY) {
                path = outPath.stringUTF8
            }
        }

        return path
    }
}
