package cpmissing

import (
	"fmt"
	"log"
	"sdmm/internal/app/command"
	"sdmm/internal/app/ui/component"
	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap/editor"

	"sdmm/internal/app/ui/dialog"
	"sdmm/internal/dmapi/dmenv"
	"sdmm/internal/dmapi/dmmap" //for the undefinedvars struct
	"sdmm/internal/dmapi/dmmap/dmminstance"
)

func (m *Missing) Init(app App) {
	m.app = app
}

type App interface {
	CurrentEditor() *editor.Editor
	DoEditInstance(*dmminstance.Instance)
	ShowLayout(name string, focus bool)
	SyncVarEditor()
	SyncPrefabs()

	CommandStorage() *command.Storage
}

type Missing struct {
	component.Component
	dmm           *dmmap.Dmm
	app           App
	index         int
	UndefinedVars []dmmap.UndefinedVar
	WorkSpaceVars map[string][]dmmap.UndefinedVar
}

type WorkSpaceUndefinedVar struct {
	WorkSpaceName string
	WorkSpaceVars map[string][]dmmap.UndefinedVar
}

func (m *Missing) LookForUndefinedVariables(dme *dmenv.Dme, data *dmmap.Dmm) (UndefinedVars []dmmap.UndefinedVar) {

	if m.WorkSpaceVars == nil {
		m.WorkSpaceVars = make(map[string][]dmmap.UndefinedVar)
	}

	for tileIndex := range data.Tiles {
		for _, instance := range data.Tiles[tileIndex].Instances() {

			prefab := instance.Prefab()
			if obj, ok := dme.Objects[prefab.Path()]; ok {
				// Prefabs from the dmmdata don't know about environment objects.
				if !prefab.Vars().HasParent() {
					prefab.Vars().LinkParent(obj.Vars)
				}
				//data.Tiles[tileIndex].InstancesAdd(dmmap.PrefabStorage.Put(prefab))
				for _, varName := range prefab.Vars().Iterate() { //start of undef
					//if the value does not exist it gives "", false. Hence _, exists
					if _, exists := obj.Vars.Value(varName); !exists {
						prefVal, _ := prefab.Vars().Value(varName)
						UndefinedVars = append(UndefinedVars, dmmap.UndefinedVar{
							Path:     prefab.Path(),
							VarName:  varName,
							VarValue: fmt.Sprintf("%v", prefVal),
							X:        instance.Coord().X, Y: instance.Coord().Y, Z: instance.Coord().Z,
							PrefabInfo: uint64(instance.Id()),
							MapName:    m.app.CurrentEditor().Dmm().Name,
						})

						log.Printf("undefined var edit:%s,  %s", prefab.Path(), prefVal)
					}
				} //end
			}
		}
	}
	if len(UndefinedVars) != 0 {
		m.WorkSpaceVars[m.app.CurrentEditor().Dmm().Name] = UndefinedVars
	}
	return UndefinedVars
}

func (m *Missing) LookForMissingTypes(dme *dmenv.Dme, data *dmmap.Dmm) (UndefinedVars []dmmap.UndefinedVar) {

	if m.WorkSpaceVars == nil {
		m.WorkSpaceVars = make(map[string][]dmmap.UndefinedVar)
	}

	for tileIndex := range data.Tiles {
		for _, instance := range data.Tiles[tileIndex].Instances() {

			prefab := instance.Prefab()
			if obj, ok := dme.Objects[prefab.Path()]; ok {
				// Prefabs from the dmmdata don't know about environment objects.
				if !prefab.Vars().HasParent() {
					prefab.Vars().LinkParent(obj.Vars)
				}
				for _, varName := range prefab.Vars().Iterate() { //start of undef
					//if the value does not exist it gives "", false. Hence _, exists
					if _, exists := obj.Vars.Value(varName); !exists {
						prefVal, _ := prefab.Vars().Value(varName)
						UndefinedVars = append(UndefinedVars, dmmap.UndefinedVar{
							Path:     prefab.Path(),
							VarName:  "MISSING", //varName,
							VarValue: fmt.Sprintf("%v", prefVal),
							X:        instance.Coord().X, Y: instance.Coord().Y, Z: instance.Coord().Z,
							PrefabInfo: uint64(instance.Id()),
						})

						log.Printf("undefined var edit:%s,  %s", prefab.Path(), prefVal)
					}
				} //end
			}
		}
	}
	if len(UndefinedVars) != 0 {
		m.WorkSpaceVars[m.app.CurrentEditor().Dmm().Name] = UndefinedVars
	}
	return UndefinedVars
}

func (m *Missing) OpenNothingMissingWindow() {
	dialog.Open(dialog.TypeInformation{
		Title:       "Nothing Missing",
		Information: "There are no missing variables.",
	})
}
