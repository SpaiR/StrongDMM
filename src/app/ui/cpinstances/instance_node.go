package cpinstances

import (
	"sort"
	"strings"

	"sdmm/dm/dmicon"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmmap/dmminstance"
)

type instanceNode struct {
	name   string
	orig   *dmminstance.Instance
	sprite *dmicon.Sprite
}

func makeInstancesNodes(content dmmdata.Content) []*instanceNode {
	var nodes []*instanceNode

	for _, instance := range content {
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

		// Fine an initial instance index.
		idx := 0
		for i, node := range nodes {
			if node.orig.Vars.Len() == 0 {
				idx = i
				break
			}
		}

		// Move the initial instance to the beginning of the slice
		initial := nodes[idx]
		nodes = append(nodes[:idx], nodes[idx+1:]...)
		nodes = append([]*instanceNode{initial}, nodes...)
	}

	return nodes
}

func makeInstanceNode(instance *dmminstance.Instance) *instanceNode {
	icon, _ := instance.Vars.Text("icon")
	iconState, _ := instance.Vars.Text("icon_state")
	dir, _ := instance.Vars.Int("dir")
	return &instanceNode{
		name:   instance.Path[strings.LastIndex(instance.Path, "/")+1:],
		orig:   instance,
		sprite: dmicon.Cache.GetSpriteOrPlaceholderV(icon, iconState, dir),
	}
}
