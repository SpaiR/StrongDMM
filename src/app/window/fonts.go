package window

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/imguiext/icon"
	"sdmm/rsc"
)

const (
	fontSizeDefault = 16

	fontSizeH1 = 32
	fontSizeH2 = 24
	fontSizeH3 = 19
)

var (
	FontDefault imgui.Font

	FontH1 imgui.Font
	FontH2 imgui.Font
	FontH3 imgui.Font
)

func (w *Window) configureFonts() {
	fontConfig := imgui.NewFontConfig()
	defer fontConfig.Delete()

	fontAtlas := imgui.CurrentIO().Fonts()
	fontAtlas.Clear()

	FontDefault = w.createFont(fontSizeDefault, fontAtlas, fontConfig)
	FontH1 = w.createFont(fontSizeH1, fontAtlas, fontConfig)
	FontH2 = w.createFont(fontSizeH2, fontAtlas, fontConfig)
	FontH3 = w.createFont(fontSizeH3, fontAtlas, fontConfig)

	imgui.CurrentIO().SetFontDefault(FontDefault)
}

func (w *Window) createFont(size float32, atlas imgui.FontAtlas, config imgui.FontConfig) (font imgui.Font) {
	font = atlas.AddFontFromMemoryTTFV(
		rsc.FontTTF(),
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
		rsc.FontIconsTTF(),
		iconSize,
		config,
		glyphsBuilder.Build().GlyphRanges,
	)

	config.SetMergeMode(false)

	return font
}
