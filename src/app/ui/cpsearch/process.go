package cpsearch

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/imguiext"
)

func (s *Search) Process() {
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

func (s *Search) showResults() {
	if len(s.results) == 0 {
		return
	}

	s.showFilterMenu()
	imgui.SameLine()
	imgui.TextDisabled("|")
	imgui.SameLine()
	imgui.Text(fmt.Sprintf("Count: %d", len(s.results)))

	if imgui.BeginChild("search_results") {
		var clipper imgui.ListClipper
		clipper.Begin(len(s.results))
		for clipper.Step() {
			for i := clipper.DisplayStart; i < clipper.DisplayEnd; i++ {
				s.showResult(s.results[i])
			}
		}
		imgui.EndChild()
	}
}

func (s *Search) showResult(i *dmminstance.Instance) {
	if imgui.Button(fmt.Sprintf("x:%03d y:%03d z:%d", i.Coord().X, i.Coord().Y, i.Coord().Z)) {
		s.app.CurrentEditor().FocusCamera(i)
	}
}

func (s *Search) showFilterMenu() {
	imgui.Button(imguiext.IconFaFilter)

	if imgui.BeginPopupContextItemV("filter_menu", imgui.PopupFlagsMouseButtonLeft) {
		// TODO: Add coords filtering
		imgui.EndPopup()
	}
}
