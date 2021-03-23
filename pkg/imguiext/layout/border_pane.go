package layout

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/pkg/imguiext"
)

type BorderPaneLayout struct {
	Top    BorderPaneAreaLayout
	Left   BorderPaneAreaLayout
	Center BorderPaneAreaLayout
	Right  BorderPaneAreaLayout
	Bottom BorderPaneAreaLayout
}

type BorderPaneAreaLayout struct {
	Content        func()
	PaddingDisable bool
}

type BorderPane struct {
	l BorderPaneLayout

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

func NewBorderPane(l BorderPaneLayout) *BorderPane {
	id := rand.Int() ^ time.Now().Nanosecond()
	return &BorderPane{
		l: l,

		topId:    fmt.Sprint("border_pane_top_", id),
		leftId:   fmt.Sprint("border_pane_left_", id),
		centerId: fmt.Sprint("border_pane_center_", id),
		rightId:  fmt.Sprint("border_pane_right_", id),
		bottomId: fmt.Sprint("border_pane_bottom_", id),
	}
}

func (b *BorderPane) Draw() {
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

func (b *BorderPane) calcTopSize(windowSize imgui.Vec2) imgui.Vec2 {
	if b.l.Center.Content == nil && b.l.Bottom.Content == nil {
		return windowSize
	}
	return imgui.Vec2{X: windowSize.X, Y: b.topSize.Y}
}

func (b *BorderPane) calcLeftSize(windowSize imgui.Vec2) imgui.Vec2 {
	return imgui.Vec2{X: b.leftSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
}

func (b *BorderPane) calcCenterSize(windowSize imgui.Vec2) imgui.Vec2 {
	return imgui.Vec2{X: windowSize.X - b.leftSize.X - b.rightSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
}

func (b *BorderPane) calcRightSize(windowSize imgui.Vec2) imgui.Vec2 {
	if b.l.Center.Content == nil {
		return imgui.Vec2{X: windowSize.X - b.leftSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
	}
	return imgui.Vec2{X: b.rightSize.X, Y: windowSize.Y - b.topSize.Y - b.bottomSize.Y}
}

func (b *BorderPane) calcBottomSize(windowSize imgui.Vec2) imgui.Vec2 {
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
