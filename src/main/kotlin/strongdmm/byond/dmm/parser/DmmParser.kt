package strongdmm.byond.dmm.parser

import java.io.File

object DmmParser {
    private const val TGM_MARKER: String = "//MAP"

    fun parse(mapFile: File): DmmData {
        return mapFile.readText(Charsets.UTF_8).let { rawMapContent ->
            val sanitizedRawMapContent = rawMapContent.replace("\r\n", "\n")
            if (rawMapContent.startsWith(TGM_MARKER)) {
                TGMParser(sanitizedRawMapContent).parse()
            } else {
                ByondParser(sanitizedRawMapContent).parse()
            }
        }
    }
}
