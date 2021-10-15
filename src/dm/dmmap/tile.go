package dmmap

import (
	"sdmm/dm"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/util"
)

type Tile struct {
	Coord   util.Point
	content dmmdata.Content
}

func (t Tile) Content() dmmdata.Content {
	return t.content
}

func (t Tile) Copy() Tile {
	return Tile{
		t.Coord,
		t.content.Copy(),
	}
}

func (t *Tile) AddInstance(instance *dmmdata.Instance) {
	t.content = append(t.content, instance)
}

func (t *Tile) RemoveInstancesByPath(pathToRemove string) {
	var newContent dmmdata.Content
	for _, instance := range t.content {
		if !dm.IsPath(instance.Path(), pathToRemove) {
			newContent = append(newContent, instance)
		}
	}
	t.content = newContent
}

func (t *Tile) Set(content dmmdata.Content) {
	t.content = content
}

// AdjustBaseContent adds missing base instances, if there are some of them.
func (t *Tile) AdjustBaseContent() {
	var hasArea, hasTurf bool
	for _, instance := range t.content {
		if dm.IsPath(instance.Path(), "/area") {
			hasArea = true
		} else if dm.IsPath(instance.Path(), "/turf") {
			hasTurf = true
		}
	}
	if !hasArea {
		t.AddInstance(baseArea)
	}
	if !hasTurf {
		t.AddInstance(baseTurf)
	}
}
