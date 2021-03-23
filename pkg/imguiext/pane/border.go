package pane

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/pkg/imguiext"
)

type BorderLayout struct {
	Top    BorderPartLayout
	Left   BorderPartLayout
	Center BorderPartLayout
	Right  BorderPartLayout
	Bottom BorderPartLayout
}

type BorderPartLayout struct {
	Content        func()
	PaddingDisable bool
}

type Border struct {
	l BorderLayout

	topId    string
	leftId   string
	centerId string
	rightId  string
	bottomId string

	topSize    imgui.Vec2
	leftSize   imgui.Vec2
	rightSize  imgui.Vec2
	bottomSize imgui.Vec2
}

func NewBorder(l BorderLayout) *Border {
	id := rand.Int() ^ time.Now().Nanosecond()
	return &Border{
		l: l,

		topId:    fmt.Sprint("border_pane_top_", id),
		leftId:   fmt.Sprint("border_pane_left_", id),
		centerId: fmt.Sprint("border_pane_center_", id),
		rightId:  fmt.Sprint("border_pane_right_", id),
		bottomId: fmt.Sprint("border_pane_bottom_", id),
	}
}

func (b *Border) Draw() {
	if b.l.Top.Content != nil {
		phantomWindow(b.topId, func() {
			b.l.Top.Content()
			b.topSize = imgui.WindowSize()
		})
	}

	if b.l.Left.Content != nil {
		phantomWindow(b.leftId, func() {
			b.l.Left.Content()
			b.leftSize = imgui.WindowSize()
		})
	}

	if b.l.Right.Content != nil {
		phantomWindow(b.rightId, func() {
			b.l.Right.Content()
			b.rightSize = imgui.WindowSize()
		})
	}

	if b.l.Bottom.Content != nil {
		phantomWindow(b.bottomId, func() {
			b.l.Bottom.Content()
			b.bottomSize = imgui.WindowSize()
		})
	}

	windowSize := imgui.WindowSize()
	initialItemSpacing := imgui.CurrentStyle().ItemSpacing()
	imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, imgui.Vec2{})

	if b.l.Top.Content != nil {
		drawContent(fmt.Sprint(b.topId), b.calcTopSize(windowSize), !b.l.Top.PaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Top.Content()
			imgui.PopStyleVar()
		})
	}

	if b.l.Left.Content != nil {
		drawContent(fmt.Sprint(b.leftId), b.calcLeftSize(windowSize), !b.l.Left.PaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Left.Content()
			imgui.PopStyleVar()
		})

		if b.l.Center.Content != nil || b.l.Right.Content != nil {
			imgui.SameLine()
		}
	}

	if b.l.Center.Content != nil {
		drawContent(b.centerId, b.calcCenterSize(windowSize), !b.l.Center.PaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Center.Content()
			imgui.PopStyleVar()
		})
	}

	if b.l.Right.Content != nil {
		if b.l.Center.Content != nil {
			imgui.SameLine()
		}

		drawContent(fmt.Sprint(b.rightId), b.calcRightSize(windowSize), !b.l.Right.PaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Right.Content()
			imgui.PopStyleVar()
		})
	}

	if b.l.Bottom.Content != nil {
		drawContent(b.bottomId, b.calcBottomSize(windowSize), !b.l.Bottom.PaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Bottom.Content()
			imgui.PopStyleVar()
		})
	}

	imgui.PopStyleVar()
}

func (b *Border) calcTopSize(windowSize imgui.Vec2) imgui.Vec2 {
	if b.l.Center.Content == nil && b.l.Bottom.Content == nil {
		return windowSize
	}
	return imgui.Vec2{X: windowSize.X, Y: b.topSize.Y}
}

func (b *Border) calcLeftSize(windowSize imgui.Vec2) imgui.Vec2 {
	return imgui.Vec2{X: b.leftSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
}

func (b *Border) calcCenterSize(windowSize imgui.Vec2) imgui.Vec2 {
	return imgui.Vec2{X: windowSize.X - b.leftSize.X - b.rightSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
}

func (b *Border) calcRightSize(windowSize imgui.Vec2) imgui.Vec2 {
	if b.l.Center.Content == nil {
		return imgui.Vec2{X: windowSize.X - b.leftSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
	}
	return imgui.Vec2{X: b.rightSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
}

func (b *Border) calcBottomSize(windowSize imgui.Vec2) imgui.Vec2 {
	if b.l.Center.Content == nil {
		return imgui.Vec2{X: windowSize.X, Y: windowSize.Y - b.topSize.Y}
	}
	return imgui.Vec2{X: windowSize.X, Y: b.bottomSize.Y}
}

func drawContent(id string, size imgui.Vec2, border bool, content func()) {
	if border {
		imgui.PushStyleColor(imgui.StyleColorBorder, imguiext.ColorZero) // make the border invisible
		imgui.PushStyleVarFloat(imgui.StyleVarChildBorderSize, 1)
	}
	imgui.BeginChildV(id, size, border, imgui.WindowFlagsNone)
	if border {
		imgui.PopStyleColor()
		imgui.PopStyleVar()
	}
	content()
	imgui.EndChild()
}

// phantomWindow is used to calculate content size.
// The window itself is fully unavailable for user: it's transparent, hidden under the docking layout and its content is disabled.
// Yes, it's the hack, but it's the only way to calculate content size to do a proper layout.
func phantomWindow(id string, content func()) {
	imgui.PushItemFlag(imgui.ItemFlagsDisabled, true)
	imgui.PushStyleVarFloat(imgui.StyleVarAlpha, 0)
	imgui.BeginV(fmt.Sprint("_phantom_", id), nil, imgui.WindowFlagsNoBringToFrontOnFocus|imgui.WindowFlagsNoMove|imgui.WindowFlagsNoDecoration)
	content()
	imgui.End()
	imgui.PopStyleVar()
	imgui.PopItemFlag()
}
