package strongdmm.byond.dmm.parser

import java.io.File

object DmmParser {
    private const val TGM_MARKER: String = "//MAP"

    fun parse(mapFile: File): DmmData {
        return mapFile.readText(Charsets.UTF_8).let { rawMapContent ->
            if (rawMapContent.startsWith(TGM_MARKER)) {
                TGMParser(rawMapContent.replace("\r\n", "\n")).parse()
            } else {
                ByondParser(rawMapContent.replace("\r\n", "\n")).parse()
            }
        }
    }
}
