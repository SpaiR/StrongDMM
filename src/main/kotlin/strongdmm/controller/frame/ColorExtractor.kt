package strongdmm.controller.frame

import strongdmm.byond.dmm.TileItem
import java.awt.Color as AWTColor

class ColorExtractor {
    companion object {
        private const val RGB_PREFIX = "rgb("
    }

    private val rgbPattern = "rgb\\((.*),(.*),(.*)\\)".toRegex()
    private val defaultColor = Color(1f, 1f, 1f, 1f)

    // Colors predefined in BYOND
    private val byondColors = arrayOf(
        "black" to "#000000",
        "silver" to "#c0c0c0",
        "gray" to "#808080", "grey" to "#808080",
        "white" to "#ffffff",
        "maroon" to "#800000",
        "red" to "#ff0000",
        "purple" to "#800080",
        "fuchsia" to "#ff00ff", "magenta" to "#ff00ff",
        "green" to "#00c000",
        "lime" to "#00ff00",
        "olive" to "#808000", "gold" to "#808000",
        "yellow" to "#ffff00",
        "navy" to "#000080",
        "blue" to "#0000ff",
        "teal" to "#008080",
        "aqua" to "#00ffff", "cyan" to "#00ffff"
    )

    fun extract(tileItem: TileItem): Color {
        var colorValue = tileItem.color

        if (colorValue.startsWith(RGB_PREFIX)) {
            colorValue = parseRGBColor(colorValue)
        }

        var awtColor: AWTColor? = null

        if (colorValue.startsWith("#")) {
            awtColor = AWTColor.decode(colorValue)
        } else if (colorValue.isNotEmpty()) {
            val hex = hexFromColorName(colorValue)
            if (hex != null) {
                awtColor = AWTColor.decode(hex)
            }
        }

        val alpha = tileItem.alpha / 255f

        if (alpha == 1f && awtColor == null) {
            return defaultColor
        }

        return if (awtColor != null) {
            Color(awtColor.red / 255f, awtColor.green / 255f, awtColor.blue / 255f, alpha)
        } else {
            Color(1f, 1f, 1f, alpha)
        }
    }

    private fun parseRGBColor(rgb: String): String {
        val rgbMatch = rgbPattern.toPattern().matcher(rgb)
        return if (rgbMatch.find()) {
            val r = rgbMatch.group(1).trim().toInt()
            val g = rgbMatch.group(2).trim().toInt()
            val b = rgbMatch.group(3).trim().toInt()
            String.format("#%02x%02x%02x", r, g, b)
        } else {
            ""
        }
    }

    private fun hexFromColorName(colorName: String): String? {
        for (byondColor in byondColors) {
            if (byondColor.first.equals(colorName, ignoreCase = true)) {
                return byondColor.second
            }
        }
        return null
    }
}
