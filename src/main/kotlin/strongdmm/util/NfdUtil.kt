package strongdmm.util

import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog
import java.io.File

object NfdUtil {
    fun selectFile(filter: String, defaultPath: String = System.getProperty("user.home")): File? {
        var file: File? = null
        val buff = MemoryUtil.memAllocPointer(1)

        if (NativeFileDialog.NFD_OpenDialog(filter, defaultPath, buff) == NativeFileDialog.NFD_OKAY) {
            file = File(buff.stringUTF8)
        }

        MemoryUtil.memFree(buff)
        return file
    }
}
