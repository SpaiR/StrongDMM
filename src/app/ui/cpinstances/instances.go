package cpinstances

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/dm/dmmap/dmminstance"
	w "sdmm/imguiext/widget"
)

type Action interface {
	AppPointSize() float32
	AppDoSelectInstance(instance dmminstance.Instance)
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

func (i *Instances) Process() {
	for _, node := range i.instanceNodes {
		isSelected := node.orig.Id() == i.selectedId

		if imgui.SelectableV(
			fmt.Sprintf("##instance_%d", node.orig.Id()),
			isSelected,
			imgui.SelectableFlagsNone,
			imgui.Vec2{Y: i.iconSize()},
		) {
			i.doSelect(node)
		}

		if isSelected && i.tmpDoScrollToInstance {
			imgui.SetScrollHereY(.5)
			i.tmpDoScrollToInstance = false
		}

		i.showContextMenu(node)

		imgui.SameLine()
		imgui.IndentV(i.textIndent())
		imgui.Text(node.name)
		imgui.UnindentV(i.textIndent())

		imgui.SameLine()
		imgui.IndentV(i.iconIndent())
		w.Image(imgui.TextureID(node.sprite.Texture()), i.iconSize(), i.iconSize()).
			Uv(
				imgui.Vec2{
					X: node.sprite.U1,
					Y: node.sprite.V1,
				},
				imgui.Vec2{
					X: node.sprite.U2,
					Y: node.sprite.V2,
				},
			).
			Build()
		imgui.UnindentV(i.iconIndent())
	}
}

func (i *Instances) Select(instance dmminstance.Instance) {
	i.instanceNodes = makeInstancesNodes(dmminstance.Cache.GetAllByPath(instance.Path))
	i.selectedId = instance.Id()
	i.tmpDoScrollToInstance = true
	log.Println("[cpinstances] selected instance id:", i.selectedId)
}

func (i *Instances) Update() {
	if i.selectedId != 0 {
		if instance, ok := dmminstance.Cache.GetById(i.selectedId); ok {
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
