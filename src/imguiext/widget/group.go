package widget

import "github.com/SpaiR/imgui-go"

type Group Layout

func (group Group) Build() {
	imgui.BeginGroup()
	for _, w := range group {
		w.Build()
	}
	imgui.EndGroup()
}
