package cpsearch

import (
	"fmt"
	"log"
	"strconv"
	"strings"

	"sdmm/app/ui/layout/lnode"
	"sdmm/app/window"
	"sdmm/dmapi/dmmap"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"

	"github.com/SpaiR/imgui-go"
)

func (s *Search) Process() {
	s.shortcuts.SetVisibleIfFocused()

	s.showControls()
	imgui.Separator()

	if len(s.results()) == 0 {
		return
	}

	s.showResultsControls()
	if imgui.BeginChild("results") {
		s.showResults()
		imgui.EndChild()
	}
}

func (s *Search) showControls() {
	w.Layout{
		w.Button(icon.Search, s.doSearch).
			Round(true).
			Tooltip("Search"),
		w.SameLine(),
		w.InputTextWithHint("##search", "Type or Prefab ID", &s.prefabId).
			ButtonClear().
			Width(-1).
			OnDeactivatedAfterEdit(func() {
				s.doSearch()
			}),
	}.Build()
}

func (s *Search) doSearch() {
	if len(s.prefabId) == 0 {
		s.Free()
		return
	}

	s.Free()

	log.Println("[cpsearch] searching for:", s.prefabId)

	if strings.HasPrefix(s.prefabId, "/") {
		for _, prefab := range dmmap.PrefabStorage.GetAllByPath(s.prefabId) {
			s.resultsAll = append(s.resultsAll, s.app.CurrentEditor().InstancesFindByPrefabId(prefab.Id())...)
		}
	} else {
		prefabId, err := strconv.ParseUint(s.prefabId, 10, 64)
		if err != nil {
			return
		}
		s.resultsAll = s.app.CurrentEditor().InstancesFindByPrefabId(prefabId)
	}

	log.Println("[cpsearch] found search results:", len(s.resultsAll))
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
	imgui.Text(fmt.Sprintf("%d/%d", s.selectedResultIdx+1, len(s.results())))
}

const resultsTableFlags = imgui.TableFlagsBordersInner | imgui.TableFlagsResizable | imgui.TableFlagsNoSavedSettings

func (s *Search) showResults() {
	if imgui.BeginTableV("search_result", 2, resultsTableFlags, imgui.Vec2{}, 0) {
		for idx, instance := range s.results() {
			if idx == s.focusedResultIdx && idx != s.lastFocusedResultIdx {
				imgui.SetScrollHereY(0)
				s.lastFocusedResultIdx = s.focusedResultIdx
			}

			imgui.TableNextColumn()

			selected := idx == s.selectedResultIdx
			if selected {
				imgui.PushStyleColor(imgui.StyleColorText, style.ColorGold)
			}
			imgui.AlignTextToFramePadding()
			imgui.Text(fmt.Sprintf("X:%03d Y:%03d Z:%d", instance.Coord().X, instance.Coord().Y, instance.Coord().Z))
			if selected {
				imgui.PopStyleColor()
			}

			imgui.TableNextColumn()

			w.Layout{
				w.Button(fmt.Sprint(icon.Search+"##jump_to_", instance.Id()), func() {
					s.jumpTo(idx, false)
				}).Round(true).Tooltip("Jump To"),
				w.SameLine(),
				w.Button(fmt.Sprint(icon.EyeDropper+"##select_", instance.Id()), func() {
					s.selectInstance(idx)
				}).Round(true).Tooltip("Select"),
			}.Build()
		}

		imgui.EndTable()
	}
}

func (s *Search) showFilterButton() {
	w.Button(icon.FilterAlt, nil).
		Round(true).
		Tooltip("Filter").
		Build()

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
	return window.PointSize() * 75
}

func (s *Search) showJumpButtons() {
	w.Layout{
		w.Button(icon.ArrowUpward, s.jumpToUp).
			Round(true).
			Tooltip("Previous Result (Shift+F3)"),
		w.SameLine(),
		w.Button(icon.ArrowDownward, s.jumpToDown).
			Round(true).
			Tooltip("Next Result (F3)"),
	}.Build()
}

func (s *Search) selectInstance(idx int) {
	instance := s.results()[idx]
	editor := s.app.CurrentEditor()
	editor.OverlaySetTileFlick(instance.Coord())
	editor.OverlaySetInstanceFlick(instance)
	s.app.ShowLayout(lnode.NameVariables, true)
	s.app.DoEditInstance(instance)
	s.selectedResultIdx = idx
}

func (s *Search) jumpTo(idx int, focus bool) {
	if idx < 0 || idx >= len(s.results()) {
		return
	}

	instance := s.results()[idx]
	editor := s.app.CurrentEditor()

	editor.FocusCamera(instance)
	editor.OverlaySetTileFlick(instance.Coord())
	editor.OverlaySetInstanceFlick(instance)

	s.selectedResultIdx = idx

	if focus {
		s.focusedResultIdx = idx
	}
}

func (s *Search) jumpToUp() {
	idx := s.selectedResultIdx - 1
	if idx < 0 {
		idx = len(s.results()) - 1
	}
	s.jumpTo(idx, true)
}

func (s *Search) jumpToDown() {
	idx := s.selectedResultIdx + 1
	if idx >= len(s.results()) {
		idx = 0
	}
	s.jumpTo(idx, true)
}
