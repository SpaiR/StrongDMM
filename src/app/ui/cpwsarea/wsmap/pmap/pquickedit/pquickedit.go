package pquickedit

import (
	"fmt"
	"github.com/SpaiR/imgui-go"
	"sdmm/app/prefs"
	"sdmm/app/window"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmvars"
	"sdmm/util"
	"strconv"
	"time"
)

type App interface {
	Prefs() prefs.Prefs
	LoadedEnvironment() *dmenv.Dme
	SelectedInstance() (*dmminstance.Instance, bool)
}

type editor interface {
	Dmm() *dmmap.Dmm
	CommitChanges(string)
	InstanceSelect(i *dmminstance.Instance)
	UpdateCanvasByCoords([]util.Point)
}

type Panel struct {
	app    App
	editor editor

	// Usable for modify by scroll.
	lastScrollEdit int64
}

func New(app App, editor editor) *Panel {
	return &Panel{
		app:    app,
		editor: editor,
	}
}

func (p *Panel) Process() {
	if selectedInstance, ok := p.app.SelectedInstance(); ok {
		p.ProcessV(selectedInstance)
	}
}

func (p *Panel) ProcessV(instance *dmminstance.Instance) {
	imgui.BeginDisabledV(!dm.IsMovable(instance.Prefab().Path()))
	p.showNudgeOption("Nudge X", true, instance)
	p.showNudgeOption("Nudge Y", false, instance)
	imgui.EndDisabled()

	p.showDirOption(instance)
}

func (p *Panel) showNudgeOption(label string, xAxis bool, instance *dmminstance.Instance) {
	var nudgeVarName string
	if p.app.Prefs().Editor.NudgeMode == prefs.SaveNudgeModePixel {
		if xAxis {
			nudgeVarName = "pixel_x"
		} else {
			nudgeVarName = "pixel_y"
		}
	} else {
		if xAxis {
			nudgeVarName = "step_x"
		} else {
			nudgeVarName = "step_y"
		}
	}

	pixelX := instance.Prefab().Vars().IntV(nudgeVarName, 0)
	value := int32(pixelX)

	onChange := func() {
		origPrefab := instance.Prefab()

		newVars := dmvars.Set(origPrefab.Vars(), nudgeVarName, strconv.Itoa(int(value)))
		newPrefab := dmmprefab.New(dmmprefab.IdNone, origPrefab.Path(), newVars)
		instance.SetPrefab(newPrefab)

		p.editor.UpdateCanvasByCoords([]util.Point{instance.Coord()})
	}
	applyChange := func() {
		p.sanitizeInstanceVar(instance, nudgeVarName, "0")
		dmmap.PrefabStorage.Put(instance.Prefab())
		p.editor.InstanceSelect(instance)
		go p.editor.CommitChanges("Quick Edit: " + label)
	}

	imgui.SetNextItemWidth(window.PointSize() * 50)
	if imgui.DragInt(label+"##"+nudgeVarName+p.editor.Dmm().Name, &value) {
		onChange()
	}

	if imgui.IsItemDeactivatedAfterEdit() {
		applyChange()
	}

	if _, mouseWheel := imgui.CurrentIO().MouseWheel(); mouseWheel != 0 && imgui.IsItemHovered() {
		if mouseWheel > 0 {
			value += 1
		} else {
			value -= 1
		}
		onChange()
		p.lastScrollEdit = time.Now().UnixMilli()
	}

	if p.isScrollEdit() {
		p.lastScrollEdit = 0
		applyChange()
	}
}

// GUI slider works by changing the value in a range of [1, maxDirs].
// While it goes like "1, 2, 3, 4" we need to actually have "1, 2, 4, 8".
// Vars below help to properly convert a "relative" value to the "real" one.
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

func (p *Panel) showDirOption(instance *dmminstance.Instance) {
	dir := instance.Prefab().Vars().IntV("dir", 0)
	value := _dirToRelativeIndex[dir]
	maxDirs := p.getIconMaxDirs(instance.Prefab().Vars())

	imgui.BeginDisabledV(maxDirs <= 1)

	label := fmt.Sprint("Dir##dir_", p.editor.Dmm().Name)

	onChange := func() {
		origPrefab := instance.Prefab()

		newDir := strconv.Itoa(_relativeIndexToDir[value])
		newVars := dmvars.Set(origPrefab.Vars(), "dir", newDir)
		newPrefab := dmmprefab.New(dmmprefab.IdNone, origPrefab.Path(), newVars)
		instance.SetPrefab(newPrefab)

		p.editor.UpdateCanvasByCoords([]util.Point{instance.Coord()})
	}
	applyChange := func() {
		p.sanitizeInstanceVar(instance, "dir", "0")
		dmmap.PrefabStorage.Put(instance.Prefab())
		p.editor.InstanceSelect(instance)
		go p.editor.CommitChanges("Quick Edit: Dir")
	}

	imgui.SetNextItemWidth(window.PointSize() * 50)
	if imgui.SliderIntV(label, &value, 1, maxDirs, fmt.Sprint(dir), imgui.SliderFlagsNone) {
		onChange()
	}

	if imgui.IsItemDeactivatedAfterEdit() {
		applyChange()
	}

	if _, mouseWheel := imgui.CurrentIO().MouseWheel(); mouseWheel != 0 && imgui.IsItemHovered() {
		initial := value
		if mouseWheel > 0 && value < maxDirs {
			value += 1
		}
		if mouseWheel < 0 && value > 1 {
			value -= 1
		}
		if initial != value {
			onChange()
			p.lastScrollEdit = time.Now().UnixMilli()
		}
	}

	if p.isScrollEdit() {
		p.lastScrollEdit = 0
		applyChange()
	}

	imgui.EndDisabled()
}

// Assume that any edition is applicable after a 500ms timeout.
// Thus, we won't create a new prefab for every mouse wheel scroll.
func (p *Panel) isScrollEdit() bool {
	return p.lastScrollEdit != 0 && time.Since(time.UnixMilli(p.lastScrollEdit)).Milliseconds() > 500
}

func (p *Panel) sanitizeInstanceVar(instance *dmminstance.Instance, varName, defaultValue string) {
	vars := instance.Prefab().Vars()
	if p.initialVarValue(instance.Prefab().Path(), varName) == vars.ValueV(varName, defaultValue) {
		vars = dmvars.Delete(vars, varName)
		instance.SetPrefab(dmmprefab.New(dmmprefab.IdNone, instance.Prefab().Path(), vars))
	}
}

func (p *Panel) initialVarValue(path, varName string) string {
	return p.app.LoadedEnvironment().Objects[path].Vars.ValueV(varName, dmvars.NullValue)
}

func (p *Panel) getIconMaxDirs(vars *dmvars.Variables) int32 {
	icon := vars.TextV("icon", "")
	iconState := vars.TextV("icon_state", "")
	state, err := dmicon.Cache.GetState(icon, iconState)
	if err != nil {
		return 0
	}
	return int32(state.Dirs)
}
