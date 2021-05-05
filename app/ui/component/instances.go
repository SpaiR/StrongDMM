package component

import (
	"fmt"
	"log"
	"sort"
	"strings"

	"github.com/SpaiR/imgui-go"
	"github.com/SpaiR/strongdmm/pkg/dm/dmicon"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
	w "github.com/SpaiR/strongdmm/pkg/imguiext/widget"
)

type InstancesAction interface {
	PointSize() float32
	DoSelectInstance(instance *dmminstance.Instance)
}

type Instances struct {
	action InstancesAction

	instanceNodes []*instanceNode
	selectedId    uint64

	tmpDoScrollToInstance bool
}

func (i *Instances) Init(action InstancesAction) {
	i.action = action
}

func (i *Instances) Free() {
	i.instanceNodes = nil
	i.selectedId = 0
}

func (i *Instances) Process() {
	for _, node := range i.instanceNodes {
		isSelected := node.orig.Id == i.selectedId

		if imgui.SelectableV(fmt.Sprintf("##instance_%d", node.orig.Id), isSelected, imgui.SelectableFlagsNone, imgui.Vec2{Y: i.iconSize()}) {
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
		w.Image(imgui.TextureID(node.sprite.Texture()), i.iconSize(), i.iconSize()).Uv(imgui.Vec2{X: node.sprite.U1, Y: node.sprite.V1}, imgui.Vec2{X: node.sprite.U2, Y: node.sprite.V2}).Build()
		imgui.UnindentV(i.iconIndent())
	}
}

func (i *Instances) Select(instance *dmminstance.Instance) {
	i.instanceNodes = makeInstancesNodes(dmminstance.Cache.GetByPath(instance.Path))
	i.selectedId = instance.Id
	i.tmpDoScrollToInstance = true
	log.Println("[component] selected instance id:", i.selectedId)
}

func (i *Instances) Update() {
	if i.selectedId != 0 {
		i.Select(dmminstance.Cache.GetById(i.selectedId))
	}
}

func (i *Instances) doSelect(node *instanceNode) {
	i.action.DoSelectInstance(node.orig)
	i.tmpDoScrollToInstance = false // do not scroll panel when we're in panel itself
}

func (i *Instances) showContextMenu(node *instanceNode) {
	// TODO: instance context menu
}

func (i Instances) iconSize() float32 {
	return 32 * i.action.PointSize()
}

func (i *Instances) textIndent() float32 {
	return 36 * i.action.PointSize()
}

func (i *Instances) iconIndent() float32 {
	return 1 * i.action.PointSize()
}

func makeInstancesNodes(instances []*dmminstance.Instance) []*instanceNode {
	var nodes []*instanceNode

	for _, instance := range instances {
		nodes = append(nodes, makeInstanceNode(instance))
	}

	if nodes != nil {
		// Group by icon_state
		sort.Slice(nodes, func(i, j int) bool {
			iIconState, _ := nodes[i].orig.Vars.Text("icon_state")
			jIconState, _ := nodes[j].orig.Vars.Text("icon_state")
			return strings.Compare(iIconState, jIconState) == -1
		})
		// Group by name
		sort.Slice(nodes, func(i, j int) bool {
			return strings.Compare(nodes[i].name, nodes[j].name) == -1
		})

		idx := 0
		for i, node := range nodes {
			if node.orig.Vars.Len() == 0 {
				idx = i
				break
			}
		}

		// Move the initial instance at the beginning of the slice
		initial := nodes[idx]
		nodes = append(nodes[:idx], nodes[idx+1:]...)
		nodes = append([]*instanceNode{initial}, nodes...)
	}

	return nodes
}

type instanceNode struct {
	name   string
	orig   *dmminstance.Instance
	sprite *dmicon.Sprite
}

func makeInstanceNode(instance *dmminstance.Instance) *instanceNode {
	icon, _ := instance.Vars.Text("icon")
	iconState, _ := instance.Vars.Text("icon_state")
	return &instanceNode{
		name:   instance.Path[strings.LastIndex(instance.Path, "/")+1:],
		orig:   instance,
		sprite: dmicon.Cache.GetSpriteOrPlaceholder(icon, iconState),
	}
}
