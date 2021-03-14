package splitter

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
)

const (
	resetSize = -1
	thickness = 2
	minSize   = 32
)

type Splitter struct {
	id string

	pointSize *float32

	sz1, sz2   float32
	splitRatio float32

	splitVertically bool
	border          bool

	prevWindowSize imgui.Vec2
}

func New(id string, pointSize *float32, splitRatio float32, splitVertically, border bool) *Splitter {
	return &Splitter{
		id: id,

		pointSize: pointSize,

		sz1: resetSize,
		sz2: resetSize,

		splitRatio:      splitRatio,
		splitVertically: splitVertically,

		border: border,
	}
}

func (s *Splitter) Draw(side1, side2 func()) {
	windowSize := imgui.WindowSize()

	if s.prevWindowSize != windowSize {
		s.sz1 = resetSize
		s.sz2 = resetSize
		s.prevWindowSize = windowSize
	}

	if s.sz1 == resetSize {
		if s.splitVertically {
			s.sz1 = windowSize.X*s.splitRatio - windowPadding().X
		} else {
			s.sz1 = windowSize.Y*s.splitRatio - windowPadding().Y
		}
	}
	if s.sz2 == resetSize {
		if s.splitVertically {
			s.sz2 = windowSize.X - s.sz1 - windowPadding().X
		} else {
			s.sz2 = windowSize.Y - s.sz1 - windowPadding().Y
		}
	}

	changed := s.splitter()

	var x, y float32
	if s.splitVertically {
		x = s.sz1 - windowPadding().X
	} else {
		y = s.sz1 - windowPadding().Y/2
	}

	imgui.BeginChildV(fmt.Sprint(s.id, "_1"), imgui.Vec2{X: x, Y: y}, s.border, imgui.WindowFlagsNone)
	if side1 != nil {
		side1()
	}
	imgui.EndChild()

	if s.splitVertically {
		imgui.SameLine()
		x = s.sz2 - windowPadding().X/2
	} else {
		x = 0
	}

	imgui.BeginChildV(fmt.Sprint(s.id, "_2"), imgui.Vec2{X: x, Y: 0}, s.border, imgui.WindowFlagsNone)
	if side2 != nil {
		side2()
	}
	imgui.EndChild()

	var size float32
	if s.splitVertically {
		size = imgui.WindowWidth()
	} else {
		size = imgui.WindowHeight()
	}

	if changed {
		s.splitRatio = s.sz1 / size
	}
}

func (s *Splitter) splitter() bool {
	windowPos := imgui.WindowPos()
	cursorPos := imgui.CursorPos()

	thickness := thickness * (*s.pointSize)

	var itemSize imgui.Vec2
	if s.splitVertically {
		itemSize = imgui.CalcItemSize(imgui.Vec2{X: thickness, Y: -1}, 0, 0)
	} else {
		itemSize = imgui.CalcItemSize(imgui.Vec2{X: -1, Y: thickness}, 0, 0)
	}

	var x, y float32
	if s.splitVertically {
		x = s.sz1 - windowPadding().X/2
	} else {
		y = s.sz1 - windowPadding().Y/2
	}

	bbMin := imgui.Vec2{
		X: windowPos.X + cursorPos.X + x,
		Y: windowPos.Y + cursorPos.Y + y,
	}
	bbMax := imgui.Vec2{
		X: bbMin.X + itemSize.X,
		Y: bbMin.Y + itemSize.Y,
	}

	var axis imgui.Axis
	if s.splitVertically {
		axis = imgui.AxisX
	} else {
		axis = imgui.AxisY
	}

	return imgui.SplitterBehavior(bbMin, bbMax, imgui.GetID(s.id), axis, &s.sz1, &s.sz2, minSize, minSize)
}

func windowPadding() imgui.Vec2 {
	return imgui.CurrentStyle().WindowPadding()
}
