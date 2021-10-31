package window

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/assets"
	"sdmm/imguiext"
)

const (
	fontSizeDefault = 14
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
		size*w.pointSize,
		config,
		atlas.GlyphRangesCyrillic(),
	)

	iconSize := (size - 2) * w.pointSize

	config.SetMergeMode(true)
	config.SetGlyphMaxAdvanceX(iconSize)

	glyphsBuilder := imgui.GlyphRangesBuilder{}
	glyphsBuilder.Add(imguiext.IconFaMin, imguiext.IconFaMax)

	atlas.AddFontFromMemoryTTFV(
		assets.FontIconsTTF(),
		iconSize,
		config,
		glyphsBuilder.Build().GlyphRanges,
	)

	config.SetMergeMode(false)

	return font
}
