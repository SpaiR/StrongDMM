package cpsearch

import (
	"fmt"
	"log"
	"strconv"
	"strings"

	"sdmm/app/ui/layout/lnode"
	"sdmm/dmapi/dmmap"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"

	"github.com/SpaiR/imgui-go"
)

func (s *Search) Process(int32) {
	if s.app.CurrentEditor() == nil {
		imgui.TextDisabled("No map opened")
		return
	}

	s.showControls()

	imgui.Separator()

	s.showResultsControls()

	imgui.Separator()

	if s.filterActive {
		s.showFilter()
		imgui.Separator()
	}

	if imgui.BeginChild("results") {
		s.showResults()
	}
	imgui.EndChild()
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
	w.Layout{
		w.Line(
			s.filterButton(),
			w.TextDisabled("|"),
			s.modifyButtons(),
		),
		w.SameLine(),
		w.Layout{
			w.AlignRight,
			w.Line(
				w.Text(fmt.Sprintf("%d/%d", s.selectedResultIdx+1, len(s.results()))),
				w.TextDisabled("|"),
				s.jumpButtons(),
			),
		},
	}.Build()
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
				w.Line(
					w.Button(fmt.Sprint(icon.Search+"##jump_to_", instance.Id()), func() {
						s.jumpTo(idx, false)
					}).Round(true).Tooltip("Jump To"),
					w.Button(fmt.Sprint(icon.EyeDropper+"##select_", instance.Id()), func() {
						s.selectInstance(idx)
					}).Round(true).Tooltip("Select"),
					w.Button(fmt.Sprint(icon.Eraser+"##delete_", instance.Id()), func() {
						s.deleteInstance(idx)
					}).Round(true).Tooltip("Delete"),
					w.Button(fmt.Sprint(icon.Repeat+"##replace_", instance.Id()), func() {
						s.replaceInstance(idx)
					}).Round(true).Tooltip("Replace with Selected"),
				),
			}.Build()
		}

		imgui.EndTable()
	}
}

func (s *Search) modifyButtons() w.Layout {
	return w.Layout{
		w.Line(
			w.Button(icon.Eraser, s.doDeleteAll).
				Round(true).
				Tooltip("Delete All"),
			w.Button(icon.Repeat, s.doReplaceAll).
				Round(true).
				Tooltip("Replace All with Selected"),
		),
	}
}

func (s *Search) doDeleteAll() {
	log.Println("[cpsearch] do delete all")
	for _, instance := range s.results() {
		s.app.CurrentEditor().InstanceDelete(instance)
	}
	s.Sync()
	s.app.CurrentEditor().CommitChanges("Delete All")
}

func (s *Search) doReplaceAll() {
	log.Println("[cpsearch] do replace all")
	if selectedPrefab, ok := s.app.CurrentEditor().SelectedPrefab(); ok {
		for _, instance := range s.results() {
			s.app.CurrentEditor().InstanceReplace(instance, selectedPrefab)
		}
		s.Sync()
		s.app.CurrentEditor().CommitChanges("Replace All")
	}
}

func (s *Search) jumpButtons() w.Layout {
	return w.Layout{
		w.Button(icon.ArrowUpward, s.jumpToUp).
			Round(true).
			Tooltip("Previous Result (Shift+F3)"),
		w.SameLine(),
		w.Button(icon.ArrowDownward, s.jumpToDown).
			Round(true).
			Tooltip("Next Result (F3)"),
	}
}

func (s *Search) selectInstance(idx int) {
	log.Println("[cpsearch] do select instance:", idx)
	instance := s.results()[idx]
	editor := s.app.CurrentEditor()
	editor.OverlaySetTileFlick(instance.Coord())
	editor.OverlaySetInstanceFlick(instance)
	s.app.ShowLayout(lnode.NameVariables, true)
	s.app.DoEditInstance(instance)
	s.selectedResultIdx = idx
}

func (s *Search) deleteInstance(idx int) {
	log.Println("[cpsearch] do delete instance:", idx)
	instance := s.results()[idx]
	editor := s.app.CurrentEditor()
	editor.InstanceDelete(instance)
	editor.CommitChanges("Delete Instance")
	s.selectedResultIdx = -1
	s.Sync()
}

func (s *Search) replaceInstance(idx int) {
	log.Println("[cpsearch] do replace instance:", idx)
	if selectedPrefab, ok := s.app.CurrentEditor().SelectedPrefab(); ok {
		instance := s.results()[idx]
		editor := s.app.CurrentEditor()
		editor.InstanceReplace(instance, selectedPrefab)
		editor.CommitChanges("Replace Instance")
		s.selectedResultIdx = -1
		s.Sync()
	}
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
