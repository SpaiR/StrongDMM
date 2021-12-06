package cpsearch

import (
	"fmt"
	"strconv"

	"github.com/SpaiR/imgui-go"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/imguiext"
)

func (s *Search) Process() {
	s.shortcuts.SetVisible(imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows))

	s.showControls()
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

	s.results = s.app.CurrentEditor().FindInstancesByPrefabId(prefabId)
	s.focusedResultIdx = -1
	s.lastFocusedResultIdx = -1
}

func (s *Search) showResults() {
	if len(s.results) == 0 {
		return
	}

	s.showResultsControls()
	imgui.Separator()
	s.showResultsList()
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
	imgui.Text(fmt.Sprintf("Count: %d", len(s.results)))
}

func (s *Search) showResultsList() {
	if imgui.BeginChild("search_results") {
		var clipper imgui.ListClipper
		clipper.Begin(len(s.results))
		for clipper.Step() {
			for i := clipper.DisplayStart; i < clipper.DisplayEnd; i++ {
				if i == s.focusedResultIdx && i != s.lastFocusedResultIdx {
					imgui.SetScrollHereY(0)
					s.lastFocusedResultIdx = s.focusedResultIdx
				}
				s.showResult(s.results[i], i)
			}
		}
		imgui.EndChild()
	}
}

func (s *Search) showResult(i *dmminstance.Instance, idx int) {
	focused := idx == s.focusedResultIdx

	if focused {
		imgui.PushStyleColor(imgui.StyleColorText, imguiext.ColorGold)
	}
	imgui.Text(fmt.Sprintf("X:%03d Y:%03d Z:%d", i.Coord().X, i.Coord().Y, i.Coord().Z))
	if focused {
		imgui.PopStyleColor()
	}

	imgui.SameLine()
	imgui.TextDisabled("|")
	imgui.SameLine()

	if imgui.Button(fmt.Sprint(imguiext.IconFaSearch+"##jump_to_", i.Id())) {
		s.jumpTo(idx)
	}
	imguiext.SetItemHoveredTooltip("Jump To")

	imgui.SameLine()

	if imgui.Button(fmt.Sprint(imguiext.IconFaEyeDropper+"##select_", i.Id())) {
		s.selectInstance(idx)
	}
	imguiext.SetItemHoveredTooltip("Select")

	imgui.Separator()
}

func (s *Search) showFilterButton() {
	imgui.Button(imguiext.IconFaFilter)

	if imgui.BeginPopupContextItemV("filter_menu", imgui.PopupFlagsMouseButtonLeft) {
		// TODO: Add coords filtering
		imgui.EndPopup()
	}
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
	s.app.DoEditInstance(s.results[idx])
	s.focusedResultIdx = idx
}

func (s *Search) jumpTo(idx int) {
	if idx < 0 || idx >= len(s.results) {
		return
	}

	instance := s.results[idx]
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
