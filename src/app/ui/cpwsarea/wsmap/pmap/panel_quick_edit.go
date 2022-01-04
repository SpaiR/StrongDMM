package pmap

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/wsmap/pmap/editor"
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

	p.showNudgeOption("Nudge X", "pixel_x", selectedInstance)
	p.showNudgeOption("Nudge Y", "pixel_y", selectedInstance)
}

func (p *panelQuickEdit) showNudgeOption(label, nudgeVarName string, instance *dmminstance.Instance) {
	pixelX := instance.Prefab().Vars().IntV(nudgeVarName, 0)
	value := int32(pixelX)

	imgui.AlignTextToFramePadding()
	imgui.Text(label)
	imgui.SameLine()
	imgui.SetNextItemWidth(p.app.PointSize() * 50)

	if imgui.DragInt("##"+nudgeVarName+p.editor.Dmm().Name, &value) {
		origPrefab := instance.Prefab()

		newVars := p.setVar(origPrefab, nudgeVarName, strconv.Itoa(int(value)))
		newPrefab := dmmprefab.New(dmmprefab.IdNone, origPrefab.Path(), newVars)
		instance.SetPrefab(newPrefab)

		p.editor.UpdateCanvasByCoords([]util.Point{instance.Coord()})
	}

	if imgui.IsItemDeactivatedAfterEdit() {
		dmmap.PrefabStorage.Put(instance.Prefab())
		p.editor.InstanceSelect(instance)
		go p.editor.CommitChanges(label)
	}
}

func (p *panelQuickEdit) setVar(prefab *dmmprefab.Prefab, varName, varValue string) (newVars *dmvars.Variables) {
	if p.initialVarValue(prefab.Path(), varName) == varValue {
		newVars = dmvars.Delete(prefab.Vars(), varValue)
	} else {
		newVars = dmvars.Set(prefab.Vars(), varName, varValue)
	}
	return newVars
}

func (p *panelQuickEdit) initialVarValue(path, varName string) string {
	return p.app.LoadedEnvironment().Objects[path].Vars.ValueV(varName, dmvars.NullValue)
}
