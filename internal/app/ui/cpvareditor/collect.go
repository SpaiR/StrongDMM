package cpvareditor

import (
	"sort"

	"sdmm/internal/dmapi/dmenv"
	"sdmm/internal/dmapi/dmvars"
	"sdmm/internal/util/slice"
)

var unmodifiableVars = []string{
	"type", "parent_type", "vars", "x", "y", "z", "filters",
	"loc", "maptext", "maptext_width", "maptext_height", "maptext_x", "maptext_y",
	"overlays", "underlays", "verbs", "appearance", "vis_locs", "vis_contents",
	"vis_flags", "bounds", "particles", "render_source", "render_target",
}

func collectVariablesNames(vars *dmvars.Variables) (variablesNames []string) {
	variablesNames = collectVariablesNames0(vars)
	sort.Strings(variablesNames)
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

func collectVariablesPaths(obj *dmenv.Object) (variablesPaths []string) {
	for {
		variablesPaths = append(variablesPaths, obj.Path)
		obj = obj.Parent()
		if obj == nil {
			break
		}
	}
	return variablesPaths
}

func collectVariablesNamesByPaths(dme *dmenv.Dme, variablesPaths []string) map[string][]string {
	variablesNamesByPaths := make(map[string][]string)

	for _, path := range variablesPaths {
		obj := dme.Objects[path]
		variablesNames := make([]string, 0, len(obj.Vars.Iterate()))

		for _, varName := range obj.Vars.Iterate() {
			if parent := obj.Parent(); parent != nil && isParentObjsHasVar0(parent, varName) {
				continue
			}
			if slice.StrContains(unmodifiableVars, varName) {
				continue
			}
			variablesNames = append(variablesNames, varName)
		}

		sort.Strings(variablesNames)
		variablesNamesByPaths[path] = variablesNames
	}

	return variablesNamesByPaths
}

func isParentObjsHasVar0(obj *dmenv.Object, varName string) bool {
	if slice.StrContains(obj.Vars.Iterate(), varName) {
		return true
	}
	if parent := obj.Parent(); parent != nil {
		return isParentObjsHasVar0(parent, varName)
	}
	return false
}
