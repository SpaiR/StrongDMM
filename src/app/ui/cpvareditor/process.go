package cpvareditor

import (
	"fmt"
	"log"
	"sort"
	"strings"

	"sdmm/platform"

	"sdmm/dmapi/dmvars"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"
	"sdmm/util/slice"

	"github.com/SpaiR/imgui-go"
)

func (v *VarEditor) Process() {
	v.shortcuts.SetVisibleIfFocused()

	if len(v.variablesNames) == 0 {
		imgui.TextDisabled("No instance/prefab selected")
		return
	}

	v.showEditModeToggle()
	v.showControls()
	imgui.BeginChild("variables")
	v.showVariables()
	imgui.EndChild()
}

func (v *VarEditor) showEditModeToggle() {
	if imgui.BeginTableV("edit_mode_toggle", 2, imgui.TableFlagsNoPadInnerX, imgui.Vec2{}, 0) {
		imgui.PushStyleVarVec2(imgui.StyleVarButtonTextAlign, imgui.Vec2{X: .5, Y: .5})

		imgui.TableNextColumn()

		if v.instance == nil {
			imgui.BeginDisabled()
		}

		v.showInstanceModeButton()

		if v.instance == nil {
			imgui.EndDisabled()
		}

		imgui.TableNextColumn()

		v.showPrefabModeButton()

		imgui.PopStyleVar()
		imgui.EndTable()
	}
}

func (v *VarEditor) showInstanceModeButton() {
	var buttonStyle w.ButtonStyle
	if v.sessionEditMode == emInstance {
		buttonStyle = style.ButtonGreen{}
	} else {
		buttonStyle = style.ButtonDefault{}
	}

	w.Button("Instance", func() {
		v.sessionEditMode = emInstance
		log.Println("[cpvareditor] set instance mode")
	}).Style(buttonStyle).Size(imgui.Vec2{X: -1}).Build()
}

func (v *VarEditor) showPrefabModeButton() {
	var buttonStyle w.ButtonStyle
	if v.sessionEditMode == emPrefab {
		buttonStyle = style.ButtonGreen{}
	} else {
		buttonStyle = style.ButtonDefault{}
	}

	w.Button("Prefab", func() {
		v.sessionEditMode = emPrefab
		log.Println("[cpvareditor] set prefab mode")
	}).Style(buttonStyle).Size(imgui.Vec2{X: -1}).Build()
}

func (v *VarEditor) showControls() {
	imgui.BeginDisabledV(v.sessionEditMode == emPrefab)
	w.Button(icon.Search, func() {
		editor := v.app.CurrentEditor()
		editor.FocusCamera(v.instance)
		editor.OverlaySetTileFlick(v.instance.Coord())
		editor.OverlaySetInstanceFlick(v.instance)
	}).Tooltip("Find Instance").Round(true).Build()
	imgui.EndDisabled()

	imgui.SameLine()

	w.Button(icon.Cog, nil).
		Round(true).
		Tooltip("Settings").
		Build()

	if imgui.BeginPopupContextItemV("var_editor_filter", imgui.PopupFlagsMouseButtonLeft) {
		cfg := v.config()

		w.Layout{
			w.MenuItem("Show modified only", v.doToggleShowModified).
				Selected(cfg.ShowModified).
				Enabled(true).
				Shortcut(platform.KeyModName(), "1"),
			w.MenuItem("Show types", v.doToggleShowByType).
				Selected(cfg.ShowByType).
				Enabled(true).
				Shortcut(platform.KeyModName(), "2"),
			w.MenuItem("Show pins", v.doToggleShowPins).
				Selected(cfg.ShowPins).
				Enabled(true).
				Shortcut(platform.KeyModName(), "3"),
		}.Build()

		imgui.EndPopup()
	}

	imgui.SameLine()

	w.InputTextWithHint("##filter_var_name", v.filterVarNameHint(), &v.filterVarName).
		ButtonClear().
		Width(-1).
		Build()

	if v.config().ShowByType {
		w.InputTextWithHint("##filter_type_name", "Filter Type", &v.filterTypeName).
			ButtonClear().
			Width(-1).
			Build()
	}
}

func (v *VarEditor) filterVarNameHint() string {
	if v.config().ShowByType {
		return "Filter Name"
	}
	return "Filter"
}

const (
	varsTableFlags = imgui.TableFlagsResizable | imgui.TableFlagsBordersInner | imgui.TableFlagsNoSavedSettings
	varsInputFlags = imgui.InputTextFlagsAutoSelectAll | imgui.InputTextFlagsEnterReturnsTrue | imgui.InputTextFlagsCtrlEnterForNewLine
)

func (v *VarEditor) showVariables() {
	if v.config().ShowByType {
		v.showVariablesByType()
	} else {
		v.showAllVariables()
	}
}

func (v *VarEditor) showVariablesByType() {
	for _, path := range v.variablesPaths {
		if v.isFilteredPath(path) {
			continue
		}

		variablesNames := v.variablesNamesByPaths[path]

		imgui.TextColored(style.ColorGold, path)
		imgui.SameLine()
		imgui.TextDisabled(fmt.Sprintf("(%d)", len(variablesNames)))

		if imgui.BeginTableV("variables", 2, varsTableFlags, imgui.Vec2{}, 0) {
			v.showVariablesNames(variablesNames)
			imgui.EndTable()
		}
	}
}

func (v *VarEditor) showAllVariables() {
	cfg := v.config()

	if len(cfg.PinnedVarNames) != 0 {
		imgui.TextColored(style.ColorGold, "Pinned")
		if imgui.BeginTableV("variables", 2, varsTableFlags, imgui.Vec2{}, 0) {
			v.showVariablesNames(cfg.PinnedVarNames)
			imgui.EndTable()
		}
		imgui.NewLine()
		imgui.TextDisabled("Other")
	}

	if imgui.BeginTableV("variables", 2, varsTableFlags, imgui.Vec2{}, 0) {
		v.showVariablesNames(v.variablesNames)
		imgui.EndTable()
	}
}

func (v *VarEditor) showVariablesNames(variablesNames []string) {
	for _, varName := range variablesNames {
		v.showVariable(varName)
	}
}

func (v *VarEditor) showVariable(varName string) {
	if v.isFilteredVariable(varName) {
		return
	}

	imgui.TableNextColumn()
	if v.config().ShowPins {
		v.showVarPin(varName)
		imgui.SameLine()
	}
	v.showVarName(varName)
	imgui.TableNextColumn()
	v.showVarInput(varName)
}

func (v *VarEditor) showVarPin(varName string) {
	cfg := v.config()
	pinned := slice.StrContains(cfg.PinnedVarNames, varName)
	if imgui.RadioButton("##var_pin_"+varName, pinned) {
		if pinned {
			cfg.PinnedVarNames = slice.StrRemove(cfg.PinnedVarNames, varName)
			v.variablesNames = append(v.variablesNames, varName)
			log.Println("[cpvareditor] variable unpinned:", varName)
		} else {
			cfg.PinnedVarNames = append(cfg.PinnedVarNames, varName)
			v.variablesNames = slice.StrRemove(v.variablesNames, varName)
			log.Println("[cpvareditor] variable pinned:", varName)
		}

		sort.Strings(cfg.PinnedVarNames)
		sort.Strings(v.variablesNames)
	}
}

func (v *VarEditor) showVarName(varName string) {
	if !v.isCurrentVarInitial(varName) {
		imgui.TextColored(style.ColorGreen3, varName)
	} else {
		imgui.Text(varName)
	}
}

func (v *VarEditor) showVarInput(varName string) {
	varValue := v.currentVars().ValueV(varName, dmvars.NullValue)
	initialValue := v.initialVarValue(varName)
	isModified := initialValue != varValue

	var resetBtn *w.ButtonWidget
	if isModified {
		resetBtn = w.Button(icon.Undo+"##"+varName, func() {
			v.setCurrentVariable(varName, initialValue)
		}).Tooltip(initialValue).Style(style.ButtonFrame{})
	}

	w.InputText(fmt.Sprint("##", v.prefab.Id(), varName), &varValue).
		Button(resetBtn).
		Width(-1).
		Flags(varsInputFlags).
		OnDeactivatedAfterEdit(func() {
			v.setCurrentVariable(varName, varValue)
		}).
		Build()
}

func (v *VarEditor) setCurrentVariable(varName, varValue string) {
	if v.sessionEditMode == emInstance {
		v.setInstanceVariable(varName, varValue)
	} else {
		v.setPrefabVariable(varName, varValue)
	}
}

func (v *VarEditor) currentVars() *dmvars.Variables {
	if v.sessionEditMode == emInstance {
		return v.instance.Prefab().Vars()
	}
	return v.prefab.Vars()
}

func (v *VarEditor) isFilteredVariable(varName string) bool {
	// Show modified only
	if v.config().ShowModified && v.isCurrentVarInitial(varName) {
		return true
	}
	// Show filtered by name only
	if len(v.filterVarName) > 0 && !strings.Contains(varName, v.filterVarName) {
		return true
	}
	return false
}

func (v *VarEditor) isFilteredPath(path string) bool {
	return v.config().ShowByType && len(v.filterTypeName) > 0 && !strings.Contains(path, v.filterTypeName)
}
