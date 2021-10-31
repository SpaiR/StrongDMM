package pmap

import (
	"sdmm/app/render/bucket/level/chunk/unit"
)

func (p *PaneMap) ProcessUnit(u unit.Unit) bool {
	if p.app.PathsFilter().IsHiddenPath(u.Instance().Prefab().Path()) {
		return false
	}
	return true
}
