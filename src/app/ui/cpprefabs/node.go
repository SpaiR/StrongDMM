package cpprefabs

import (
	"sort"
	"strings"

	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap/dmmdata"
)

type node struct {
	name   string
	orig   *dmmdata.Prefab
	sprite *dmicon.Sprite
}

func makeNodes(content dmmdata.Content) []*node {
	var nodes []*node

	for _, prefab := range content {
		nodes = append(nodes, makeNode(prefab))
	}

	if nodes != nil {
		// Group by icon_state
		sort.Slice(nodes, func(i, j int) bool {
			iIconState, _ := nodes[i].orig.Vars().Text("icon_state")
			jIconState, _ := nodes[j].orig.Vars().Text("icon_state")
			return strings.Compare(iIconState, jIconState) == -1
		})
		// Group by name
		sort.Slice(nodes, func(i, j int) bool {
			return strings.Compare(nodes[i].name, nodes[j].name) == -1
		})

		// Fine an initial prefab index.
		idx := 0
		for i, node := range nodes {
			if node.orig.Vars().Len() == 0 {
				idx = i
				break
			}
		}

		// Move the initial prefab to the beginning of the slice
		initial := nodes[idx]
		nodes = append(nodes[:idx], nodes[idx+1:]...)
		nodes = append([]*node{initial}, nodes...)
	}

	return nodes
}

func makeNode(prefab *dmmdata.Prefab) *node {
	icon, _ := prefab.Vars().Text("icon")
	iconState, _ := prefab.Vars().Text("icon_state")
	dir, _ := prefab.Vars().Int("dir")
	return &node{
		name:   prefab.Path()[strings.LastIndex(prefab.Path(), "/")+1:],
		orig:   prefab,
		sprite: dmicon.Cache.GetSpriteOrPlaceholderV(icon, iconState, dir),
	}
}
