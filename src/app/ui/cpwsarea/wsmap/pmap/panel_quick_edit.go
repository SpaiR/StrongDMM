package pmap

import (
	"fmt"
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/wsmap/pmap/editor"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmvars"
	"sdmm/util"
	"strconv"
)

type panelQuickEdit struct {
	app    App
	editor *editor.Editor
}

func (p *panelQuickEdit) process() {
	selectedInstance, ok := p.app.SelectedInstance()
	if !ok {
		return
	}

	imgui.BeginDisabledV(!dm.IsMovable(selectedInstance.Prefab().Path()))
	p.showNudgeOption("Nudge X", "pixel_x", selectedInstance)
	p.showNudgeOption("Nudge Y", "pixel_y", selectedInstance)
	imgui.EndDisabled()

	p.showDirOption(selectedInstance)
}

func (p *panelQuickEdit) showNudgeOption(label, nudgeVarName string, instance *dmminstance.Instance) {
	pixelX := instance.Prefab().Vars().IntV(nudgeVarName, 0)
	value := int32(pixelX)

	imgui.SetNextItemWidth(p.app.PointSize() * 50)
	if imgui.DragInt(label+"##"+nudgeVarName+p.editor.Dmm().Name, &value) {
		origPrefab := instance.Prefab()

		newVars := dmvars.Set(origPrefab.Vars(), nudgeVarName, strconv.Itoa(int(value)))
		newPrefab := dmmprefab.New(dmmprefab.IdNone, origPrefab.Path(), newVars)
		instance.SetPrefab(newPrefab)

		p.editor.UpdateCanvasByCoords([]util.Point{instance.Coord()})
	}

	if imgui.IsItemDeactivatedAfterEdit() {
		p.sanitizeInstanceVar(instance, nudgeVarName, "0")
		dmmap.PrefabStorage.Put(instance.Prefab())
		p.editor.InstanceSelect(instance)
		go p.editor.CommitChanges("Quick Edit: " + label)
	}
}

// GUI slider works by changing the value in a range of [1, maxDirs].
// While it goes like "1, 2, 3, 4" we need to actually have "1, 2, 4, 8".
// Maps below help to properly convert a "relative" value to the "real" one.
var (
	_relativeIndexToDir = map[int32]int{
		1: dm.DirNorth,
		2: dm.DirSouth,
		3: dm.DirEast,
		4: dm.DirWest,
		5: dm.DirNortheast,
		6: dm.DirSouthwest,
		7: dm.DirNorthwest,
		8: dm.DirSoutheast,
	}
	_dirToRelativeIndex = map[int]int32{
		dm.DirNorth:     1,
		dm.DirSouth:     2,
		dm.DirEast:      3,
		dm.DirWest:      4,
		dm.DirNortheast: 5,
		dm.DirSouthwest: 6,
		dm.DirNorthwest: 7,
		dm.DirSoutheast: 8,
	}
)

func (p *panelQuickEdit) showDirOption(instance *dmminstance.Instance) {
	dir := instance.Prefab().Vars().IntV("dir", 0)
	value := _dirToRelativeIndex[dir]
	maxDirs := p.getIconMaxDirs(instance.Prefab().Vars())

	imgui.BeginDisabledV(maxDirs <= 1)

	label := fmt.Sprint("Dir##dir_", p.editor.Dmm().Name)

	imgui.SetNextItemWidth(p.app.PointSize() * 50)
	if imgui.SliderIntV(label, &value, 1, maxDirs, fmt.Sprint(dir), imgui.SliderFlagsNone) {
		origPrefab := instance.Prefab()

		newDir := _relativeIndexToDir[value]
		newVars := dmvars.Set(origPrefab.Vars(), "dir", strconv.Itoa(newDir))
		newPrefab := dmmprefab.New(dmmprefab.IdNone, origPrefab.Path(), newVars)
		instance.SetPrefab(newPrefab)

		p.editor.UpdateCanvasByCoords([]util.Point{instance.Coord()})
	}

	if imgui.IsItemDeactivatedAfterEdit() {
		p.sanitizeInstanceVar(instance, "dir", "0")
		dmmap.PrefabStorage.Put(instance.Prefab())
		p.editor.InstanceSelect(instance)
		go p.editor.CommitChanges("Quick Edit: Dir")
	}

	imgui.EndDisabled()
}

func (p *panelQuickEdit) sanitizeInstanceVar(instance *dmminstance.Instance, varName, defaultValue string) {
	vars := instance.Prefab().Vars()
	if p.initialVarValue(instance.Prefab().Path(), varName) == vars.ValueV(varName, defaultValue) {
		vars = dmvars.Delete(vars, varName)
		instance.SetPrefab(dmmprefab.New(dmmprefab.IdNone, instance.Prefab().Path(), vars))
	}
}

func (p *panelQuickEdit) initialVarValue(path, varName string) string {
	return p.app.LoadedEnvironment().Objects[path].Vars.ValueV(varName, dmvars.NullValue)
}

func (p *panelQuickEdit) getIconMaxDirs(vars *dmvars.Variables) int32 {
	icon := vars.TextV("icon", "")
	iconState := vars.TextV("icon_state", "")
	state, err := dmicon.Cache.GetState(icon, iconState)
	if err != nil {
		return 0
	}
	return int32(state.Dirs)
}
