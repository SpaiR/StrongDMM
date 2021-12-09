package cpvareditor

import (
	"sort"
	"strings"

	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmvars"
	"sdmm/util/slice"
)

type App interface {
	DoSelectPrefab(prefab *dmmprefab.Prefab)
	ToggleShortcuts(enabled bool)
	CurrentEditor() *pmap.Editor
}

type editMode int

const (
	emInstance editMode = iota
	emPrefab
)

type VarEditor struct {
	app App

	instance *dmminstance.Instance
	prefab   *dmmprefab.Prefab

	variablesNames []string

	sessionEditMode editMode
	sessionPrefabId uint64
}

func (v *VarEditor) Init(app App) {
	v.app = app
}

func (v *VarEditor) Free() {
	v.resetSession()
}

// Sync does the check if we edit an instance which is exists.
// If the instance doesn't exist, then the editor will switch its mode to the prefab editing.
func (v *VarEditor) Sync() {
	if v.prefab == nil {
		return
	}
	if v.instance != nil && !v.app.CurrentEditor().Dmm().IsInstanceExist(v.instance.Id()) {
		v.instance = nil
		v.sessionEditMode = emPrefab
	}
}

func (v *VarEditor) EditInstance(instance *dmminstance.Instance) {
	v.resetSession()
	v.sessionEditMode = emInstance
	v.instance = instance
	v.setup(instance.Prefab())
}

func (v *VarEditor) EditPrefab(prefab *dmmprefab.Prefab) {
	v.resetSession()
	v.sessionEditMode = emPrefab
	v.setup(prefab)
}

func (v *VarEditor) setup(prefab *dmmprefab.Prefab) {
	v.prefab = prefab
	v.variablesNames = collectVariablesNames(prefab.Vars())
}

func (v *VarEditor) setInstanceVariable(varName, varValue string) {
	if len(varValue) == 0 {
		varValue = dmvars.NullValue
	}

	origPrefab := v.instance.Prefab()
	newPrefab, isNew := dmmap.PrefabStorage.GetV(origPrefab.Path(), dmvars.Modify(origPrefab.Vars(), varName, varValue))

	// Newly created prefabs are sort of temporal objects, which need to exist only during the edit session.
	// So if we modified a variable of the instance and that creates a new prefab, the previous one will be deleted.
	if isNew {
		if origPrefab.Id() == v.sessionPrefabId {
			dmmap.PrefabStorage.Delete(origPrefab)
		}
		v.sessionPrefabId = newPrefab.Id()
	}

	v.instance.SetPrefab(newPrefab)
	v.app.CurrentEditor().CommitChanges("Edit Variable")
	v.app.DoSelectPrefab(newPrefab)

	v.prefab = newPrefab
}

func (v *VarEditor) setPrefabVariable(varName, varValue string) {
	if len(varValue) == 0 {
		varValue = dmvars.NullValue
	}

	newPrefab := dmmap.PrefabStorage.Get(v.prefab.Path(), dmvars.Modify(v.prefab.Vars(), varName, varValue))

	v.app.CurrentEditor().ReplacePrefab(v.prefab, newPrefab)
	v.app.DoSelectPrefab(newPrefab)

	v.prefab = newPrefab
}

func (v *VarEditor) resetSession() {
	v.variablesNames = nil
	v.sessionPrefabId = 0
	v.sessionEditMode = emPrefab
	v.instance = nil
	v.prefab = nil
}

var unmodifiableVars = []string{
	"type", "parent_type", "vars", "x", "y", "z", "contents", "filters",
	"loc", "maptext", "maptext_width", "maptext_height", "maptext_x", "maptext_y",
	"overlays", "underlays", "verbs", "appearance", "vis_locs",
}

func collectVariablesNames(vars *dmvars.Variables) (variablesNames []string) {
	variablesNames = collectVariablesNames0(vars)
	sort.Slice(variablesNames, func(i, j int) bool {
		return strings.Compare(variablesNames[i], variablesNames[j]) == -1
	})
	return variablesNames
}

func collectVariablesNames0(vars *dmvars.Variables) []string {
	variablesNames := make([]string, 0, vars.Len())
	for _, varName := range vars.Iterate() {
		if !slice.StrContains(unmodifiableVars, varName) {
			variablesNames = append(variablesNames, varName)
		}
	}
	if vars.HasParent() {
		for _, parentVarName := range collectVariablesNames0(vars.Parent()) {
			variablesNames = slice.StrPushUnique(variablesNames, parentVarName)
		}
	}
	return variablesNames
}
