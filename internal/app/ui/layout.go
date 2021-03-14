package ui

import (
	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/ui/component"
	"github.com/SpaiR/strongdmm/pkg/imguiext/splitter"
)

type action interface {
	component.EnvironmentAction

	PointSizePtr() *float32
}

type Layout struct {
	component.Environment

	spLeftCenter *splitter.Splitter
}

func NewLayout(a action) *Layout {
	l := &Layout{
		spLeftCenter: splitter.New("left_center", a.PointSizePtr(), .15, true, false),
	}
	l.Environment.Init(a)
	return l
}

func (l *Layout) Process() {
	vp := imgui.MainViewport()

	imgui.SetNextWindowPos(vp.WorkPos())
	imgui.SetNextWindowSize(vp.WorkSize())

	imgui.BeginV("layout", nil, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsNoMove|imgui.WindowFlagsNoResize|imgui.WindowFlagsNoBringToFrontOnFocus)
	l.spLeftCenter.Draw(l.Environment.Process, nil)
	imgui.End()
}
