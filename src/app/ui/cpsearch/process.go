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

	imgui.Text("Columns:")
	imgui.SameLine()
	if imgui.SmallButton(imguiext.IconFaMinus) {
		if cfg := s.config(); cfg.Columns > 1 {
			cfg.Columns--
		}
	}
	imgui.SameLine()
	if imgui.SmallButton(imguiext.IconFaPlus) {
		s.config().Columns++
	}

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
