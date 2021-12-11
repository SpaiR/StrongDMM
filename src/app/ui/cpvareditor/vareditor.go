package cpvareditor

import (
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap"
	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmvars"
)

type App interface {
	DoSelectPrefab(prefab *dmmprefab.Prefab)
	ToggleShortcuts(enabled bool)
	CurrentEditor() *pmap.Editor
	LoadedEnvironment() *dmenv.Dme
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

	variablesPaths        []string
	variablesNamesByPaths map[string][]string

	sessionEditMode editMode
	sessionPrefabId uint64

	filterVarName  string
	filterTypeName string

	showModified bool
	showByType   bool
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
	v.variablesPaths = collectVariablesPaths(v.app.LoadedEnvironment().Objects[v.prefab.Path()])
	v.variablesNamesByPaths = collectVariablesNamesByPaths(v.app.LoadedEnvironment(), v.variablesPaths)
}

func (v *VarEditor) setInstanceVariable(varName, varValue string) {
	if len(varValue) == 0 {
		varValue = dmvars.NullValue
	}

	origPrefab := v.instance.Prefab()

	var newVars *dmvars.Variables
	if v.initialVarValue(varName) == varValue {
		newVars = dmvars.Delete(origPrefab.Vars(), varName)
	} else {
		newVars = dmvars.Set(origPrefab.Vars(), varName, varValue)
	}

	newPrefab, isNew := dmmap.PrefabStorage.GetV(origPrefab.Path(), newVars)

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

	var newVars *dmvars.Variables
	if v.initialVarValue(varName) == varValue {
		newVars = dmvars.Delete(v.prefab.Vars(), varName)
	} else {
		newVars = dmvars.Set(v.prefab.Vars(), varName, varValue)
	}

	newPrefab := dmmap.PrefabStorage.Get(v.prefab.Path(), newVars)

	v.app.CurrentEditor().ReplacePrefab(v.prefab, newPrefab)
	v.app.DoSelectPrefab(newPrefab)

	v.prefab = newPrefab
}

func (v *VarEditor) resetSession() {
	v.variablesNames = nil
	v.variablesPaths = nil
	v.variablesNamesByPaths = nil
	v.sessionPrefabId = 0
	v.sessionEditMode = emPrefab
	v.instance = nil
	v.prefab = nil
}

func (v *VarEditor) initialVarValue(varName string) string {
	return v.app.LoadedEnvironment().Objects[v.prefab.Path()].Vars.ValueV(varName, dmvars.NullValue)
}

func (v *VarEditor) isCurrentVarInitial(varName string) bool {
	return v.currentVars().ValueV(varName, dmvars.NullValue) == v.initialVarValue(varName)
}
