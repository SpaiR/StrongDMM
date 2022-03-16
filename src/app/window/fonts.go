package window

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/imguiext/icon"
	"sdmm/rsc"
)

const (
	fontSizeH1 = 32
	fontSizeH2 = 24
	fontSizeH3 = 19
	fontSizeH4 = 16
)

var (
	FontDefault imgui.Font

	FontH1 imgui.Font
	FontH2 imgui.Font
	FontH3 imgui.Font
)

func configureFonts() {
	fontConfig := imgui.NewFontConfig()
	defer fontConfig.Delete()

	fontAtlas := imgui.CurrentIO().Fonts()
	fontAtlas.Clear()

	FontDefault = createFont(fontSizeH4, fontAtlas, fontConfig)

	FontH1 = createFont(fontSizeH1, fontAtlas, fontConfig)
	FontH2 = createFont(fontSizeH2, fontAtlas, fontConfig)
	FontH3 = createFont(fontSizeH3, fontAtlas, fontConfig)

	imgui.CurrentIO().SetFontDefault(FontDefault)
}

func createFont(size float32, atlas imgui.FontAtlas, config imgui.FontConfig) (font imgui.Font) {
	fontSize := size * pointSize

	font = atlas.AddFontFromMemoryTTFV(
		rsc.FontTTF(),
		fontSize,
		config,
		atlas.GlyphRangesCyrillic(),
	)

	config.SetMergeMode(true)
	config.SetPixelSnapH(true)
	config.SetGlyphOffsetY(2)
	config.SetGlyphMaxAdvanceX(fontSize)

	glyphsBuilder := imgui.GlyphRangesBuilder{}
	glyphsBuilder.Add(icon.RangeMin, icon.RangeMax)

	atlas.AddFontFromMemoryTTFV(
		rsc.FontIconsTTF(),
		fontSize,
		config,
		glyphsBuilder.Build().GlyphRanges,
	)

	config.SetMergeMode(false)

	return font
}
