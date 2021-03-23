package pane

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/pkg/imguiext"
)

type BorderLayout struct {
	Top    func()
	Left   func()
	Center func()
	Right  func()
	Bottom func()

	TopPaddingDisable    bool
	LeftPaddingDisable   bool
	CenterPaddingDisable bool
	RightPaddingDisable  bool
	BottomPaddingDisable bool
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
	if b.l.Top != nil {
		fakeWindow(b.topId, func() {
			b.l.Top()
			b.topSize = imgui.WindowSize()
		})
	}

	if b.l.Left != nil {
		fakeWindow(b.leftId, func() {
			b.l.Left()
			b.leftSize = imgui.WindowSize()
		})
	}

	if b.l.Right != nil {
		fakeWindow(b.rightId, func() {
			b.l.Right()
			b.rightSize = imgui.WindowSize()
		})
	}

	if b.l.Bottom != nil {
		fakeWindow(b.bottomId, func() {
			b.l.Bottom()
			b.bottomSize = imgui.WindowSize()
		})
	}

	windowSize := imgui.WindowSize()
	initialItemSpacing := imgui.CurrentStyle().ItemSpacing()
	imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, imgui.Vec2{})

	if b.l.Top != nil {
		window(fmt.Sprint(b.topId), b.calcTopSize(windowSize), !b.l.TopPaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Top()
			imgui.PopStyleVar()
		})
	}

	if b.l.Left != nil {
		window(fmt.Sprint(b.leftId), b.calcLeftSize(windowSize), !b.l.LeftPaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Left()
			imgui.PopStyleVar()
		})

		if b.l.Center != nil || b.l.Right != nil {
			imgui.SameLine()
		}
	}

	if b.l.Center != nil {
		window(b.centerId, b.calcCenterSize(windowSize), !b.l.CenterPaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Center()
			imgui.PopStyleVar()
		})
	}

	if b.l.Right != nil {
		if b.l.Center != nil {
			imgui.SameLine()
		}

		window(fmt.Sprint(b.rightId), b.calcRightSize(windowSize), !b.l.RightPaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Right()
			imgui.PopStyleVar()
		})
	}

	if b.l.Bottom != nil {
		window(b.bottomId, b.calcBottomSize(windowSize), !b.l.BottomPaddingDisable, func() {
			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, initialItemSpacing)
			b.l.Bottom()
			imgui.PopStyleVar()
		})
	}

	imgui.PopStyleVar()
}

func (b *Border) calcTopSize(windowSize imgui.Vec2) imgui.Vec2 {
	if b.l.Center == nil && b.l.Bottom == nil {
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
	if b.l.Center == nil {
		return imgui.Vec2{X: windowSize.X - b.leftSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
	}
	return imgui.Vec2{X: b.rightSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
}

func (b *Border) calcBottomSize(windowSize imgui.Vec2) imgui.Vec2 {
	if b.l.Center == nil {
		return imgui.Vec2{X: windowSize.X, Y: windowSize.Y - b.topSize.Y}
	}
	return imgui.Vec2{X: windowSize.X, Y: b.bottomSize.Y}
}

func window(id string, size imgui.Vec2, border bool, content func()) {
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

// fakeWindow is used to calculate the content of parts size of which we don't know.
// The window itself is hidden under the docking layout and its content is disabled.
// Yes, it's a hack, but imgui is doesn't provide an option to calculate content size manually.
func fakeWindow(id string, content func()) {
	imgui.BeginV(fmt.Sprint("fake_", id), nil, imgui.WindowFlagsNoBringToFrontOnFocus|imgui.WindowFlagsNoMove|imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
	imgui.PushItemFlag(imgui.ItemFlagsDisabled, true)
	content()
	imgui.PopItemFlag()
	imgui.End()
}
