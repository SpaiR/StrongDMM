package cpvareditor

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
	"sdmm/dmapi/dmvars"
	"sdmm/imguiext"
	w "sdmm/imguiext/widget"
)

func (v *VarEditor) Process() {
	if len(v.variablesNames) == 0 {
		imgui.TextDisabled("No Instance/Prefab Selected")
		return
	}

	v.showEditModeToggle()
	v.showControls()
	imgui.Separator()
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
		buttonStyle = imguiext.StyleButtonGreen{}
	} else {
		buttonStyle = imguiext.StyleButtonDefault{}
	}

	w.Button("Instance", func() {
		v.sessionEditMode = emInstance
	}).Style(buttonStyle).Size(imgui.Vec2{X: -1}).Build()
}

func (v *VarEditor) showPrefabModeButton() {
	var buttonStyle w.ButtonStyle
	if v.sessionEditMode == emPrefab {
		buttonStyle = imguiext.StyleButtonGreen{}
	} else {
		buttonStyle = imguiext.StyleButtonDefault{}
	}

	w.Button("Prefab", func() {
		v.sessionEditMode = emPrefab
	}).Style(buttonStyle).Size(imgui.Vec2{X: -1}).Build()
}

func (v *VarEditor) showControls() {
	imgui.SetNextItemWidth(-1)
	imgui.InputTextWithHint("##filter", "Filter", &v.filter)

	imgui.Checkbox("Modified", &v.showModified)
	imgui.SameLine()
	imgui.Checkbox("By Type", &v.showByType)
}

const (
	varsTableFlags = imgui.TableFlagsResizable | imgui.TableFlagsBordersInner
	varsInputFlags = imgui.InputTextFlagsAutoSelectAll | imgui.InputTextFlagsEnterReturnsTrue | imgui.InputTextFlagsCtrlEnterForNewLine
)

func (v *VarEditor) showVariables() {
	if v.showByType {
		v.showVariablesByType()
	} else {
		v.showAllVariables()
	}
}

func (v *VarEditor) showVariablesByType() {
	for _, path := range v.variablesPaths {
		variablesNames := v.variablesNamesByPaths[path]

		imgui.TextColored(imguiext.ColorGold, path)
		imgui.SameLine()
		imgui.TextDisabled(fmt.Sprintf("(%d)", len(variablesNames)))

		if imgui.BeginTableV("variables", 2, varsTableFlags, imgui.Vec2{}, 0) {
			v.showVariablesNames(variablesNames)
			imgui.EndTable()
		}
	}
}

func (v *VarEditor) showAllVariables() {
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
	v.showVarName(varName)
	imgui.TableNextColumn()
	v.showVarInput(varName)
}

func (v *VarEditor) showVarName(varName string) {
	if !v.isCurrentVarInitial(varName) {
		imgui.TextColored(imguiext.ColorGreen3, varName)
	} else {
		imgui.Text(varName)
	}
}

func (v *VarEditor) showVarInput(varName string) {
	imgui.SetNextItemWidth(-1)

	varValue := v.currentVars().ValueV(varName, dmvars.NullValue)

	imgui.InputTextV(fmt.Sprint("##", v.prefab.Id(), varName), &varValue, varsInputFlags, nil)
	if imgui.IsItemDeactivatedAfterEdit() {
		if v.sessionEditMode == emInstance {
			v.setInstanceVariable(varName, varValue)
		} else {
			v.setPrefabVariable(varName, varValue)
		}
	}
	if imgui.IsItemActivated() {
		v.app.ToggleShortcuts(false)
	} else if imgui.IsItemDeactivated() {
		v.app.ToggleShortcuts(true)
	}
}

func (v *VarEditor) currentVars() *dmvars.Variables {
	if v.sessionEditMode == emInstance {
		return v.instance.Prefab().Vars()
	}
	return v.prefab.Vars()
}
