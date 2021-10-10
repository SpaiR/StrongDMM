package dmmap

import (
	"sdmm/dm"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmmap/dmminstance"
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

func (t *Tile) AddInstance(instance *dmminstance.Instance) {
	t.content = append(t.content, instance)
}

func (t *Tile) RemoveInstancesByPath(pathToRemove string) {
	var newContent dmmdata.Content
	for _, instance := range t.content {
		if !dm.IsPath(instance.Path, pathToRemove) {
			newContent = append(newContent, instance)
		}
	}
	t.content = newContent
}

func (t *Tile) Set(content dmmdata.Content) {
	t.content = content
}
