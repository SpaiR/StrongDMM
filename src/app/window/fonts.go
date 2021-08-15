package window

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/assets"
	"sdmm/imguiext"
)

const (
	fontSizeDefault = 15
)

func (w *Window) configureFonts() {
	fontConfig := imgui.NewFontConfig()
	defer fontConfig.Delete()

	fontConfig.SetFontBuilderFlags(imgui.FreeTypeBuilderFlagsLightHinting)

	fontAtlas := imgui.CurrentIO().Fonts()
	fontAtlas.Clear()

	font := w.createFont(fontSizeDefault, fontAtlas, fontConfig)

	imgui.CurrentIO().SetFontDefault(font)
}

func (w *Window) createFont(size float32, atlas imgui.FontAtlas, config imgui.FontConfig) (font imgui.Font) {
	font = atlas.AddFontFromMemoryTTFV(
		assets.FontTTF(),
		size*w.PointSize,
		config,
		atlas.GlyphRangesCyrillic(),
	)

	iconSize := (size - 2) * w.PointSize

	config.SetMergeMode(true)
	config.SetGlyphMaxAdvanceX(iconSize)

	glyphsBuilder := imgui.GlyphRangesBuilder{}
	glyphsBuilder.Add(imguiext.IconFaMin, imguiext.IconFaMax)

	glyphs := glyphsBuilder.Build()
	defer glyphs.Free()

	atlas.AddFontFromMemoryTTFV(
		assets.IconsTTF(),
		iconSize,
		config,
		glyphs.GlyphRanges,
	)

	config.SetMergeMode(false)

	return font
}
