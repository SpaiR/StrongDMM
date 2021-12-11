package window

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/assets"
	"sdmm/imguiext/icon"
)

const (
	fontSizeDefault = 14
)

func (w *Window) configureFonts() {
	fontConfig := imgui.NewFontConfig()
	defer fontConfig.Delete()

	fontConfig.SetFontBuilderFlags(freeTypeBuilderFlagsLightHinting)

	fontAtlas := imgui.CurrentIO().Fonts()
	fontAtlas.Clear()

	font := w.createFont(fontSizeDefault, fontAtlas, fontConfig)

	imgui.CurrentIO().SetFontDefault(font)
}

func (w *Window) createFont(size float32, atlas imgui.FontAtlas, config imgui.FontConfig) (font imgui.Font) {
	font = atlas.AddFontFromMemoryTTFV(
		assets.FontTTF(),
		size*w.pointSize,
		config,
		atlas.GlyphRangesCyrillic(),
	)

	iconSize := (size - 2) * w.pointSize

	config.SetMergeMode(true)
	config.SetGlyphMaxAdvanceX(iconSize)

	glyphsBuilder := imgui.GlyphRangesBuilder{}
	glyphsBuilder.Add(icon.FaMin, icon.FaMax)

	atlas.AddFontFromMemoryTTFV(
		assets.FontIconsTTF(),
		iconSize,
		config,
		glyphsBuilder.Build().GlyphRanges,
	)

	config.SetMergeMode(false)

	return font
}

// Basically a copy-paste of th freetype flags from the original imgui-go repo.
// Needed to avoid boilerplate around custom build-constraints around "imguifreetype" tag,
// which is required to build imgui-go with enabled freetype font rendering.
const (
	// FreeTypeBuilderFlagsNoHinting disables hinting.
	// This generally generates 'blurrier' bitmap glyphs when the glyph are rendered in any of the anti-aliased modes.
	freeTypeBuilderFlagsNoHinting = 1 << 0

	// FreeTypeBuilderFlagsNoAutoHint disables auto-hinter.
	freeTypeBuilderFlagsNoAutoHint = 1 << 1

	// FreeTypeBuilderFlagsForceAutoHint indicates that the auto-hinter is preferred over the font's native hinter.
	freeTypeBuilderFlagsForceAutoHint = 1 << 2

	// FreeTypeBuilderFlagsLightHinting is a lighter hinting algorithm for gray-level modes.
	// Many generated glyphs are fuzzier but better resemble their original shape.
	// This is achieved by snapping glyphs to the pixel grid only vertically (Y-axis),
	// as is done by Microsoft's ClearType and Adobe's proprietary font renderer.
	// This preserves inter-glyph spacing in horizontal text.
	freeTypeBuilderFlagsLightHinting = 1 << 3

	// FreeTypeBuilderFlagsMonoHinting is a strong hinting algorithm that should only be used for monochrome output.
	freeTypeBuilderFlagsMonoHinting = 1 << 4

	// FreeTypeBuilderFlagsBold is for styling: Should we artificially embolden the font?
	freeTypeBuilderFlagsBold = 1 << 5

	// FreeTypeBuilderFlagsOblique is for styling: Should we slant the font, emulating italic style?
	freeTypeBuilderFlagsOblique = 1 << 6

	// FreeTypeBuilderFlagsMonochrome disables anti-aliasing. Combine this with MonoHinting for best results!
	freeTypeBuilderFlagsMonochrome = 1 << 7

	// FreeTypeBuilderFlagsLoadColor enables FreeType color-layered glyphs.
	freeTypeBuilderFlagsLoadColor = 1 << 8

	// FreeTypeBuilderFlagsBitmap enables FreeType bitmap glyphs
	freeTypeBuilderFlagsBitmap = 1 << 9
)
