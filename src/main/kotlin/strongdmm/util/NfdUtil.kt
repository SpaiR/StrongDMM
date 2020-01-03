package strongdmm.util

import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog
import strongdmm.util.inline.AbsPath

object NfdUtil {
    fun selectFile(filter: String, defaultPath: String = System.getProperty("user.home")): AbsPath? {
        var path: AbsPath? = null
        val buff = MemoryUtil.memAllocPointer(1)

        if (NativeFileDialog.NFD_OpenDialog(filter, defaultPath, buff) == NativeFileDialog.NFD_OKAY) {
            path = AbsPath(buff.stringUTF8)
        }

        MemoryUtil.memFree(buff)
        return path
    }
}
