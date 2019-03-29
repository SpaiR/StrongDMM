package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.logic.map.TileItem
import java.awt.Color as AWTColor

private val RGB_PATTERN = "rgb\\((.*),(.*),(.*)\\)".toRegex()
private const val RGB_PREFIX = "rgb("

fun extractColor(tileItem: TileItem): Color {
    var colorValue = tileItem.color

    if (colorValue.startsWith(RGB_PREFIX)) {
        colorValue = parseRGBColor(colorValue)
    }

    var awtColor: AWTColor? = null

    if (colorValue.startsWith("#")) {
        awtColor = AWTColor.decode(colorValue)
    } else if (!colorValue.isEmpty()) {
        val hex = ByondColor.hexFromColorName(colorValue)
        if (hex != null) {
            awtColor = AWTColor.decode(hex)
        }
    }

    val alpha = tileItem.alpha / 255f

    if (alpha == 1f && awtColor == null) {
        return DEFAULT_COLOR
    }

    return if (awtColor != null) {
        Color(awtColor.red / 255f, awtColor.green / 255f, awtColor.blue / 255f, alpha)
    } else {
        Color(alpha = alpha)
    }
}

private fun parseRGBColor(rgb: String): String {
    val rgbMatch = RGB_PATTERN.toPattern().matcher(rgb)
    return if (rgbMatch.find()) {
        val r = rgbMatch.group(1).trim().toInt()
        val g = rgbMatch.group(2).trim().toInt()
        val b = rgbMatch.group(3).trim().toInt()
        String.format("#%02x%02x%02x", r, g, b)
    } else {
        ""
    }
}

// Colors predefined in BYOND
enum class ByondColor constructor(private val hex: String) {

    BLACK("#000000"),
    SILVER("#c0c0c0"),
    GRAY("#808080"), GREY(GRAY.hex),
    WHITE("#ffffff"),
    MAROON("#800000"),
    RED("#ff0000"),
    PURPLE("#800080"),
    FUCHSIA("#ff00ff"), MAGENTA(FUCHSIA.hex),
    GREEN("#00c000"),
    LIME("#00ff00"),
    OLIVE("#808000"),
    GOLD("#808000"),
    YELLOW("#ffff00"),
    NAVY("#000080"),
    BLUE("#0000ff"),
    TEAL("#008080"),
    AQUA("#00ffff"),
    CYAN("#00ffff");

    companion object {
        fun hexFromColorName(colorName: String): String? {
            for (byondColor in values()) {
                if (byondColor.name.equals(colorName, true)) {
                    return byondColor.hex
                }
            }
            return null
        }
    }
}
