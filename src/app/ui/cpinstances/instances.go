package cpinstances

import (
	"log"

	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmmdata"
)

type Action interface {
	AppPointSize() float32
	AppDoSelectInstance(instance *dmmdata.Instance)
}

type Instances struct {
	action Action

	instanceNodes []*instanceNode
	selectedId    uint64

	tmpDoScrollToInstance bool
}

func (i *Instances) Init(action Action) {
	i.action = action
}

func (i *Instances) Free() {
	i.instanceNodes = nil
	i.selectedId = 0
}

func (i *Instances) Select(instance *dmmdata.Instance) {
	i.instanceNodes = makeInstancesNodes(dmmap.InstanceCache.GetAllByPath(instance.Path))
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
	i.action.AppDoSelectInstance(node.orig)
	i.tmpDoScrollToInstance = false // do not scroll panel when we're in panel itself
}

func (i *Instances) showContextMenu(node *instanceNode) {
	// TODO: instance context menu
}

func (i Instances) iconSize() float32 {
	return 32 * i.action.AppPointSize()
}

func (i *Instances) textIndent() float32 {
	return 36 * i.action.AppPointSize()
}

func (i *Instances) iconIndent() float32 {
	return 1 * i.action.AppPointSize()
}
