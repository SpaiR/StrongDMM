package widget

import "github.com/SpaiR/imgui-go"

type imageWidget struct {
	texture                imgui.TextureID
	size                   imgui.Vec2
	uv0, uv1               imgui.Vec2
	tintColor, borderColor imgui.Vec4
}

func (i *imageWidget) Uv(uv0, uv1 imgui.Vec2) *imageWidget {
	i.uv0, i.uv1 = uv0, uv1
	return i
}

func (i *imageWidget) TintColor(tintColor imgui.Vec4) *imageWidget {
	i.tintColor = tintColor
	return i
}

func (i *imageWidget) BorderColor(borderColor imgui.Vec4) *imageWidget {
	i.borderColor = borderColor
	return i
}

func (i *imageWidget) Build() {
	imgui.ImageV(i.texture, i.size, i.uv0, i.uv1, i.tintColor, i.borderColor)
}

func Image(texture imgui.TextureID, width, height float32) *imageWidget {
	return &imageWidget{
		texture:     texture,
		size:        imgui.Vec2{X: width, Y: height},
		uv0:         imgui.Vec2{},
		uv1:         imgui.Vec2{X: 1, Y: 1},
		tintColor:   imgui.Vec4{X: 1, Y: 1, Z: 1, W: 1},
		borderColor: imgui.Vec4{},
	}
}
