package strongdmm.util

import kool.use
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog
import strongdmm.util.inline.AbsPath

object NfdUtil {
    fun selectFile(filter: String, defaultPath: String = System.getProperty("user.home")): AbsPath? {
        var path: AbsPath? = null

        MemoryUtil.memAllocPointer(1).use { outPath ->
            if (NativeFileDialog.NFD_OpenDialog(filter, defaultPath, outPath) == NativeFileDialog.NFD_OKAY) {
                path = AbsPath(outPath.stringUTF8)
            }
        }

        return path
    }
}
