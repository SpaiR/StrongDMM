package strongdmm.util.inline

import java.io.File

inline class AbsPath(val value: String) {
    constructor(file: File) : this(file.absolutePath)

    override fun toString(): String = value
}
