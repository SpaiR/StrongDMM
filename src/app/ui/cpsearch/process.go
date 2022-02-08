package cpsearch

import (
	"fmt"
	"log"
	"strconv"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/layout/lnode"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"
)

func (s *Search) Process() {
	s.shortcuts.SetVisibleIfFocused()

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
	w.InputTextWithHint("##search", "Prefab ID", &s.prefabId).
		ButtonClear().
		Width(-1).
		Build()
}

func (s *Search) doSearch() {
	if len(s.prefabId) == 0 {
		s.Free()
		return
	}

	prefabId, err := strconv.ParseUint(s.prefabId, 10, 64)
	if err != nil {
		return
	}

	s.Free()

	log.Println("[cpsearch] searching for:", s.prefabId)
	s.resultsAll = s.app.CurrentEditor().InstancesFindByPrefabId(prefabId)
	log.Println("[cpsearch] found search results:", len(s.resultsAll))

	if len(s.resultsAll) > 0 {
		s.jumpTo(0)
	}
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
	imgui.Text(fmt.Sprintf("%d/%d", s.focusedResultIdx+1, len(s.results())))
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
		imgui.PushStyleColor(imgui.StyleColorText, style.ColorGold)
	}
	imgui.Text(fmt.Sprintf("X:%03d Y:%03d Z:%d", instance.Coord().X, instance.Coord().Y, instance.Coord().Z))
	if focused {
		imgui.PopStyleColor()
	}

	imgui.SameLine()
	imgui.TextDisabled("|")
	imgui.SameLine()

	if imgui.Button(fmt.Sprint(icon.FaSearch+"##jump_to_", instance.Id())) {
		s.jumpTo(idx)
	}
	imguiext.SetItemHoveredTooltip("Jump To")

	imgui.SameLine()

	if imgui.Button(fmt.Sprint(icon.FaEyeDropper+"##select_", instance.Id())) {
		s.selectInstance(idx)
	}
	imguiext.SetItemHoveredTooltip("Select")

	imgui.Separator()
}

func (s *Search) showFilterButton() {
	imgui.Button(icon.FaFilter)

	if imgui.BeginPopupContextItemV("filter_menu", imgui.PopupFlagsMouseButtonLeft) {
		imgui.Text("Bounds")

		s.inputBound("X1:", &s.filterBoundX1)
		imgui.SameLine()
		s.inputBound("Y1:", &s.filterBoundY1)
		s.inputBound("X2:", &s.filterBoundX2)
		imgui.SameLine()
		s.inputBound("Y2:", &s.filterBoundY2)

		w.Button("Reset", s.doResetFilter).
			Style(style.ButtonRed{}).
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
	if imgui.Button(icon.FaArrowUp) {
		s.jumpToUp()
	}
	imguiext.SetItemHoveredTooltip("Previous Result (Shift+F3)")
	imgui.SameLine()
	if imgui.Button(icon.FaArrowDown) {
		s.jumpToDown()
	}
	imguiext.SetItemHoveredTooltip("Next Result (F3)")
}

func (s *Search) selectInstance(idx int) {
	instance := s.results()[idx]
	editor := s.app.CurrentEditor()
	editor.OverlaySetTileFlick(instance.Coord())
	editor.OverlaySetInstanceFlick(instance)
	s.app.ShowLayout(lnode.NameVariables, true)
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
	editor.OverlaySetTileFlick(instance.Coord())
	editor.OverlaySetInstanceFlick(instance)

	s.focusedResultIdx = idx
}

func (s *Search) jumpToUp() {
	idx := s.focusedResultIdx - 1
	if idx < 0 {
		idx = len(s.results()) - 1
	}
	s.jumpTo(idx)
}

func (s *Search) jumpToDown() {
	idx := s.focusedResultIdx + 1
	if idx >= len(s.results()) {
		idx = 0
	}
	s.jumpTo(idx)
}
