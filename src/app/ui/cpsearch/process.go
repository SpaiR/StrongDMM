package cpsearch

import (
	"fmt"
	"strconv"

	"github.com/SpaiR/imgui-go"
	"sdmm/imguiext"
	w "sdmm/imguiext/widget"
)

func (s *Search) Process() {
	s.shortcuts.SetVisible(imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows))

	s.showControls()
	imgui.Separator()

	if len(s.results()) == 0 {
		return
	}

	s.showResultsControls()
	imgui.Separator()
	s.showResults()
}

func (s *Search) showControls() {
	if imgui.Button("Search") {
		s.doSearch()
	}
	imgui.SameLine()
	imgui.SetNextItemWidth(-1)
	imgui.InputTextWithHint("##search", "Prefab ID", &s.prefabId)
}

func (s *Search) doSearch() {
	if len(s.prefabId) == 0 {
		return
	}

	prefabId, err := strconv.ParseUint(s.prefabId, 10, 64)
	if err != nil {
		return
	}

	s.Free()

	s.resultsAll = s.app.CurrentEditor().FindInstancesByPrefabId(prefabId)
}

func (s *Search) showResultsControls() {
	s.showFilterButton()
	imgui.SameLine()
	imgui.TextDisabled("|")
	imgui.SameLine()
	s.showJumpButtons()
	imgui.SameLine()
	imgui.TextDisabled("|")
	imgui.SameLine()
	imgui.Text(fmt.Sprintf("Count: %d", len(s.results())))
}

func (s *Search) showResults() {
	if imgui.BeginChild("search_results") {
		var clipper imgui.ListClipper
		clipper.Begin(len(s.results()))
		for clipper.Step() {
			for idx := clipper.DisplayStart; idx < clipper.DisplayEnd; idx++ {
				if idx == s.focusedResultIdx && idx != s.lastFocusedResultIdx {
					imgui.SetScrollHereY(0)
					s.lastFocusedResultIdx = s.focusedResultIdx
				}
				s.showResult(idx)
			}
		}

		imgui.EndChild()
	}
}

func (s *Search) showResult(idx int) {
	instance := s.results()[idx]
	focused := idx == s.focusedResultIdx

	if focused {
		imgui.PushStyleColor(imgui.StyleColorText, imguiext.ColorGold)
	}
	imgui.Text(fmt.Sprintf("X:%03d Y:%03d Z:%d", instance.Coord().X, instance.Coord().Y, instance.Coord().Z))
	if focused {
		imgui.PopStyleColor()
	}

	imgui.SameLine()
	imgui.TextDisabled("|")
	imgui.SameLine()

	if imgui.Button(fmt.Sprint(imguiext.IconFaSearch+"##jump_to_", instance.Id())) {
		s.jumpTo(idx)
	}
	imguiext.SetItemHoveredTooltip("Jump To")

	imgui.SameLine()

	if imgui.Button(fmt.Sprint(imguiext.IconFaEyeDropper+"##select_", instance.Id())) {
		s.selectInstance(idx)
	}
	imguiext.SetItemHoveredTooltip("Select")

	imgui.Separator()
}

func (s *Search) showFilterButton() {
	imgui.Button(imguiext.IconFaFilter)

	if imgui.BeginPopupContextItemV("filter_menu", imgui.PopupFlagsMouseButtonLeft) {
		imgui.Text("Bounds")

		s.inputBound("X1:", &s.filterBoundX1)
		imgui.SameLine()
		s.inputBound("Y1:", &s.filterBoundY1)
		s.inputBound("X2:", &s.filterBoundX2)
		imgui.SameLine()
		s.inputBound("Y2:", &s.filterBoundY2)

		w.Button("Reset", s.doResetFilter).
			Style(imguiext.StyleButtonRed{}).
			Build()

		imgui.EndPopup()
	}
}

func (s *Search) inputBound(label string, v *int32) {
	imgui.Text(label)
	imgui.SameLine()
	imgui.SetNextItemWidth(s.inputBoundWidth())
	if imgui.InputInt("##"+label, v) {
		s.updateFilteredResults()
	}
}

func (s *Search) inputBoundWidth() float32 {
	return s.app.PointSize() * 75
}

func (s *Search) showJumpButtons() {
	if imgui.Button(imguiext.IconFaArrowUp) {
		s.jumpToUp()
	}
	imguiext.SetItemHoveredTooltip("Previous Result (Shift+F3)")
	imgui.SameLine()
	if imgui.Button(imguiext.IconFaArrowDown) {
		s.jumpToDown()
	}
	imguiext.SetItemHoveredTooltip("Next Result (F3)")
}

func (s *Search) selectInstance(idx int) {
	instance := s.results()[idx]
	editor := s.app.CurrentEditor()
	editor.MarkFlickTile(instance.Coord())
	editor.MarkFlickInstance(instance)
	s.app.DoEditInstance(instance)
	s.focusedResultIdx = idx
}

func (s *Search) jumpTo(idx int) {
	if idx < 0 || idx >= len(s.results()) {
		return
	}

	instance := s.results()[idx]
	editor := s.app.CurrentEditor()

	editor.FocusCamera(instance)
	editor.MarkFlickTile(instance.Coord())
	editor.MarkFlickInstance(instance)

	s.focusedResultIdx = idx
}

func (s *Search) jumpToUp() {
	s.jumpTo(s.focusedResultIdx - 1)
}

func (s *Search) jumpToDown() {
	s.jumpTo(s.focusedResultIdx + 1)
}
