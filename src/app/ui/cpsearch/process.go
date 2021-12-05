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

const resultsTableFlags = imgui.TableFlagsBordersV | imgui.TableFlagsResizable

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
		if imgui.BeginTableV("results_table", int(s.config().Columns), resultsTableFlags, imgui.Vec2{}, 0) {
			for _, result := range s.results {
				imgui.TableNextColumn()
				s.showResult(result)
			}
			imgui.EndTable()
		}

		imgui.EndChild()
	}
}

func (s *Search) showResult(i *dmminstance.Instance) {
	if imgui.Button(fmt.Sprintf("x:%03d y:%03d z:%d", i.Coord().X, i.Coord().Y, i.Coord().Z)) {

	}
}

func (s *Search) showFilterMenu() {
	imgui.Button(imguiext.IconFaFilter)

	if imgui.BeginPopupContextItemV("filter_menu", imgui.PopupFlagsMouseButtonLeft) {
		cfg := s.config()

		cols := int32(cfg.Columns)
		imgui.Text("Columns")
		imgui.SameLine()
		imgui.SetNextItemWidth(imgui.WindowWidth() / 2)
		if imguiext.InputIntClamp("##columns", &cols, 1, 64, 1, 10) {
			cfg.Columns = uint(cols)
		}

		imgui.EndPopup()
	}
}
