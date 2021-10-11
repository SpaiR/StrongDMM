package cpinstances

import (
	"log"

	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmmdata"
)

type App interface {
	PointSize() float32
	DoSelectInstance(instance *dmmdata.Instance)
}

type Instances struct {
	app App

	instanceNodes []*instanceNode
	selectedId    uint64

	tmpDoScrollToInstance bool
}

func (i *Instances) Init(app App) {
	i.app = app
}

func (i *Instances) Free() {
	i.instanceNodes = nil
	i.selectedId = 0
}

func (i *Instances) Select(instance *dmmdata.Instance) {
	i.instanceNodes = makeInstancesNodes(dmmap.InstanceCache.GetAllByPath(instance.Path()))
	i.selectedId = instance.Id()
	i.tmpDoScrollToInstance = true
	log.Println("[cpinstances] selected instance id:", i.selectedId)
}

func (i *Instances) Update() {
	if i.selectedId != 0 {
		if instance, ok := dmmap.InstanceCache.GetById(i.selectedId); ok {
			i.Select(instance)
		}
	}
}

// SelectedInstanceId returns the id of the instance currently selected in the Instances panel.
func (i *Instances) SelectedInstanceId() uint64 {
	return i.selectedId
}

func (i *Instances) doSelect(node *instanceNode) {
	i.app.DoSelectInstance(node.orig)
	i.tmpDoScrollToInstance = false // do not scroll panel when we're in panel itself
}

func (i *Instances) showContextMenu(node *instanceNode) {
	// TODO: instance context menu
}

func (i Instances) iconSize() float32 {
	return 32 * i.app.PointSize()
}

func (i *Instances) textIndent() float32 {
	return 36 * i.app.PointSize()
}

func (i *Instances) iconIndent() float32 {
	return 1 * i.app.PointSize()
}
