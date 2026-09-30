package cpmissing

import (
	"fmt"
	"log"
	"sdmm/internal/app/command"
	"sdmm/internal/dmapi/dmmap" //for the undefinedvars struct
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/dmapi/dmvars"
	"sdmm/internal/imguiext/icon"
	w "sdmm/internal/imguiext/widget"
	"sdmm/internal/util"

	"github.com/SpaiR/imgui-go"
)

func (m *Missing) Process(int32) {
	m.ShowControls()
}

func (m *Missing) removeUndefinedVariableFromPrefab(undef dmmap.UndefinedVar, i int) {
	tile := m.app.CurrentEditor().Dmm().GetTile(util.Point{X: undef.X, Y: undef.Y, Z: undef.Z})
	for _, instance := range tile.Instances() {
		if instance.Id() == undef.PrefabInfo {
			prefab := instance.Prefab()
			vars := prefab.Vars()
			for _, varName := range prefab.Vars().Iterate() {
				if undef.VarName == varName {

					//the doing part
					wsName := m.app.CurrentEditor().Dmm().Name
					vars = dmvars.Delete(vars, varName)
					instance.SetPrefab(dmmprefab.New(dmmprefab.IdNone, prefab.Path(), vars))

					m.app.SyncVarEditor()
					if m.app.CurrentEditor() != nil {
						m.app.CurrentEditor().FocusCameraOnPosition(instance.Coord())
					}
					m.app.CurrentEditor().InstanceSelect(instance)
					m.UndefinedVars = append(m.UndefinedVars[:i], m.UndefinedVars[i+1:]...)

					if wsVars, exists := m.WorkSpaceVars[wsName]; exists {
						// match variable inside the WorkSpaceVars-Undefined vars to what we just removed
						for idx, v := range wsVars {
							// match?
							if v.Path == undef.Path && v.VarName == undef.VarName && v.PrefabInfo == undef.PrefabInfo {
								wsVars = append(wsVars[:idx], wsVars[idx+1:]...)
								break
							}
						}

						if len(wsVars) == 0 {
							delete(m.WorkSpaceVars, wsName)
						} else {
							m.WorkSpaceVars[wsName] = wsVars
						}
					}

					//registers the redo and undo
					m.app.CommandStorage().Push(command.Make("Removed Undefined", func() {
						vars = dmvars.Set(vars, varName, undef.VarValue)
						instance.SetPrefab(dmmprefab.New(dmmprefab.IdNone, prefab.Path(), vars))
						m.app.SyncVarEditor()
						m.UndefinedVars = append(m.UndefinedVars[:i], append([]dmmap.UndefinedVar{undef}, m.UndefinedVars[i:]...)...)

						if m.WorkSpaceVars == nil {
							m.WorkSpaceVars = make(map[string][]dmmap.UndefinedVar)
						}

						m.WorkSpaceVars[wsName] = append(m.WorkSpaceVars[wsName], undef)

					}, func() {
						vars = dmvars.Delete(vars, varName)
						instance.SetPrefab(dmmprefab.New(dmmprefab.IdNone, prefab.Path(), vars))
						m.app.SyncVarEditor()
						m.UndefinedVars = append(m.UndefinedVars[:i], m.UndefinedVars[i+1:]...)

						if wsVars, exists := m.WorkSpaceVars[wsName]; exists {
							for idx, v := range wsVars {
								if v.Path == undef.Path && v.VarName == undef.VarName && v.PrefabInfo == undef.PrefabInfo {
									wsVars = append(wsVars[:idx], wsVars[idx+1:]...)
									break
								}
							}
							if len(wsVars) == 0 {
								delete(m.WorkSpaceVars, wsName)
							} else {
								m.WorkSpaceVars[wsName] = wsVars
							}
						}
					}))

				}
			}
		}
	}
}

func (m *Missing) removeUnknownPrefabEntry(undef dmmap.UndefinedVar, i int) {
	tile := m.app.CurrentEditor().Dmm().GetTile(util.Point{X: undef.X, Y: undef.Y, Z: undef.Z})
	replacementPrefab, prefabSelected := m.app.CurrentEditor().SelectedPrefab()
	if prefabSelected == true {
		tile.InstancesAdd(replacementPrefab)
		tile.InstancesRegenerate()
		if m.app.CurrentEditor() != nil {
			m.app.CurrentEditor().UpdateCanvasByCoords([]util.Point{tile.Coord})
		}
		m.UndefinedVars = append(m.UndefinedVars[:i], m.UndefinedVars[i+1:]...)
		if len(m.UndefinedVars) != 0 {
			m.WorkSpaceVars[undef.MapName] = m.UndefinedVars
		} else {
			delete(m.WorkSpaceVars, m.app.CurrentEditor().Dmm().Name)
		}
		//registers the redo and undo
		m.app.CommandStorage().Push(command.Make("Replaced Unknown Type", func() {
			tile.InstancesRemoveByInstance(tile.Instances()[len(tile.Instances())-1])
			tile.InstancesRegenerate()
			if m.app.CurrentEditor() != nil {
				m.app.CurrentEditor().UpdateCanvasByCoords([]util.Point{tile.Coord})
			}
			m.UndefinedVars = append(m.UndefinedVars[:i], append([]dmmap.UndefinedVar{undef}, m.UndefinedVars[i:]...)...)
			if len(m.UndefinedVars) != 0 {
				m.WorkSpaceVars[undef.MapName] = m.UndefinedVars
			} else {
				delete(m.WorkSpaceVars, m.app.CurrentEditor().Dmm().Name)
			}

		}, func() {
			tile.InstancesAdd(replacementPrefab)
			tile.InstancesRegenerate()
			if m.app.CurrentEditor() != nil {
				m.app.CurrentEditor().UpdateCanvasByCoords([]util.Point{tile.Coord})
			}
			m.UndefinedVars = append(m.UndefinedVars[:i], m.UndefinedVars[i+1:]...)
			if len(m.UndefinedVars) != 0 {
				m.WorkSpaceVars[undef.MapName] = m.UndefinedVars
			} else {
				delete(m.WorkSpaceVars, m.app.CurrentEditor().Dmm().Name)
			}
		}))
		log.Printf("test")
	}
}

func (m *Missing) ShowControls() {
	if imgui.BeginChild("results") {
		tableFlags := imgui.TableFlagsResizable |
			imgui.TableFlagsSizingStretchProp |
			imgui.TableFlagsBordersInnerH
		imgui.BeginTableV("Undefined Variables", 4, tableFlags, imgui.Vec2{X: 0, Y: 0}, 0.0)
		imgui.TableSetupColumn("")
		imgui.TableSetupColumn("")
		imgui.TableSetupColumn("")
		imgui.TableSetupColumnV("", imgui.TableColumnFlagsWidthFixed, 0.0, 0)
		for i, undef := range m.UndefinedVars {
			imgui.TableNextRow()
			imgui.TableSetColumnIndex(0)
			imgui.Text(undef.Path)

			imgui.TableSetColumnIndex(1)
			coords := fmt.Sprintf("X:%d Y:%d Z:%d", undef.X, undef.Y, undef.Z)
			imgui.Text(coords)

			imgui.TableSetColumnIndex(2)
			imgui.Text(undef.VarName)

			imgui.TableSetColumnIndex(3)
			imgui.BeginGroup()
			buttonLabelJump := fmt.Sprintf(icon.Search+"##%d", i)
			buttonLabelNullorReplace := fmt.Sprintf(icon.Clear+"##%d", i)
			if undef.VarName == "MISSINGPREFAB" {
				buttonLabelNullorReplace = fmt.Sprintf(icon.Repeat+"##%d", i)
			}
			w.Layout{
				w.Button(buttonLabelJump, func() {
					if m.app.CurrentEditor() != nil {
						m.app.CurrentEditor().FocusCameraOnPosition(util.Point{X: undef.X, Y: undef.Y, Z: undef.Z})
					}
					jumptile := m.app.CurrentEditor().Dmm().GetTile(util.Point{X: undef.X, Y: undef.Y, Z: undef.Z})
					for _, instance := range jumptile.Instances() {
						if instance.Id() == undef.PrefabInfo {
							m.app.CurrentEditor().InstanceSelect(instance)
						}
					}
				}).Small(true),
				w.SameLine(),
				w.Button(buttonLabelNullorReplace, func() {
					if undef.VarName == "MISSINGPREFAB" {
						m.removeUnknownPrefabEntry(undef, i)
					} else {
						m.removeUndefinedVariableFromPrefab(undef, i)
					}

				}).Small(true),
			}.Build()
			imgui.EndGroup()

		}
		imgui.EndTable()
		if imgui.Button("Dismiss") {

			wsName := m.UndefinedVars[0].MapName
			delete(m.WorkSpaceVars, wsName)
			m.UndefinedVars = nil
		}
		imgui.EndChild()
	}
}
