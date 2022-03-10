package widget

import "github.com/SpaiR/imgui-go"

type alignTextToFramePaddingWidget struct {
}

func (alignTextToFramePaddingWidget) Build() {
	imgui.AlignTextToFramePadding()
}

func AlignTextToFramePadding() *alignTextToFramePaddingWidget {
	return &alignTextToFramePaddingWidget{}
}
