package cpsearch

import (
	"log"
	"math"

	"sdmm/app/ui/cpwsarea/wsmap/tools"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"
	"sdmm/util"

	"github.com/SpaiR/imgui-go"
)

func (s *Search) filterButton() w.Layout {
	var bntStyle w.ButtonStyle
	if s.filterActive {
		bntStyle = style.ButtonGreen{}
	} else {
		bntStyle = style.ButtonDefault{}
	}

	return w.Layout{
		w.Button(icon.FilterAlt, s.doToggleFilter).
			Style(bntStyle).
			Round(true),
		w.Tooltip(w.AlignTextToFramePadding(), w.Line(w.Text("Filter"), w.TextFrame("F"))),
	}
}

func (s *Search) showFilter() {
	s.fetchGrabToolFilterBounds()

	imgui.AlignTextToFramePadding()

	imgui.TextDisabled(icon.Help)
	imguiext.SetItemHoveredTooltip(
		"Filter results with bounds\n" +
			"Control with sliders: X1, Y1, X2, Y2\n" +
			"Or select an area with the \"Grab\" tool",
	)

	imgui.SameLine()

	w.Button(icon.Delete+"##reset_results", s.doResetFilter).
		Style(style.ButtonRed{}).
		Tooltip("Reset").
		Build()

	imgui.SameLine()

	var bounds [4]int32
	bounds[0] = int32(s.filterBound.X1)
	bounds[1] = int32(s.filterBound.Y1)
	bounds[2] = int32(s.filterBound.X2)
	bounds[3] = int32(s.filterBound.Y2)

	max := math.Max(float64(s.app.CurrentEditor().Dmm().MaxX), float64(s.app.CurrentEditor().Dmm().MaxY))

	imgui.SetNextItemWidth(-1)
	if imgui.SliderInt4("##bounds", &bounds, 0, int(max)) {
		s.filterBound.X1 = float32(bounds[0])
		s.filterBound.Y1 = float32(bounds[1])
		s.filterBound.X2 = float32(bounds[2])
		s.filterBound.Y2 = float32(bounds[3])
		s.updateFilteredResults()
	}
}

func (s *Search) doToggleFilter() {
	s.filterActive = !s.filterActive

	log.Println("[cpsearch] filter toggled:", s.filterActive)

	if !s.filterActive {
		s.doResetFilter()
	}
}

func (s *Search) fetchGrabToolFilterBounds() {
	if !tools.IsSelected(tools.TNGrab) {
		return
	}

	grab := tools.Selected().(*tools.ToolGrab)

	if !grab.HasSelectedArea() {
		return
	}

	if s.filterBound != grab.Bounds() {
		s.filterBound = grab.Bounds()
		s.updateFilteredResults()
	}
}

func (s *Search) doResetFilter() {
	s.resultsFiltered = s.resultsFiltered[:0]
	s.filterBound = util.Bounds{}
	log.Println("[cpsearch] search filter reset")
}

func (s *Search) updateFilteredResults() {
	s.resultsFiltered = s.resultsFiltered[:0]
	for _, result := range s.resultsAll {
		if s.filterBound.Contains(float32(result.Coord().X), float32(result.Coord().Y)) {
			s.resultsFiltered = append(s.resultsFiltered, result)
		}
	}
}
