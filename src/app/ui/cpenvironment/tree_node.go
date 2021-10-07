package cpenvironment

import (
	"strings"

	"sdmm/dm/dmenv"
	"sdmm/dm/dmicon"
)

type treeNode struct {
	name   string
	orig   *dmenv.Object
	sprite *dmicon.Sprite
}

func (e *Environment) treeNode(object *dmenv.Object) (*treeNode, bool) {
	if node, ok := e.treeNodes[object.Path]; ok {
		return node, true
	}

	if e.tmpNewTreeNodesCount >= newTreeNodesLimit {
		return nil, false
	}

	e.tmpNewTreeNodesCount += 1

	icon, _ := object.Vars.Text("icon")
	iconState, _ := object.Vars.Text("icon_state")

	node := &treeNode{
		name:   object.Path[strings.LastIndex(object.Path, "/")+1:],
		orig:   object,
		sprite: dmicon.Cache.GetSpriteOrPlaceholder(icon, iconState),
	}

	e.treeNodes[object.Path] = node
	return node, true
}
