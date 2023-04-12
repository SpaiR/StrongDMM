package editor

import (
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/util"
)

type AreaZone struct {
	Name    string
	Borders []AreaBorder
}

type AreaBorder struct {
	Coord util.Point
	Dirs  int
}

var zoneDirs = map[util.Point]int{
	util.Point{X: 1}:  dm.DirEast,
	util.Point{X: -1}: dm.DirWest,
	util.Point{Y: 1}:  dm.DirNorth,
	util.Point{Y: -1}: dm.DirSouth,
}

func (e *Editor) updateAreasZones() {
	type coords map[util.Point]bool

	areas := make(map[string]coords)

	for _, tile := range e.dmm.Tiles {
		for _, instance := range tile.Instances() {
			if path := instance.Prefab().Path(); dm.IsPath(path, "/area") {
				if _, ok := areas[path]; !ok {
					areas[path] = make(coords)
				}
				areas[path][tile.Coord] = true
			}
		}
	}

	var areaZones []AreaZone

	for areaName, areaCoords := range areas {
		areaZone := AreaZone{Name: areaName}

		for coord := range areaCoords {
			var areaBorder AreaBorder

			for shift, dir := range zoneDirs {
				if _, ok := areaCoords[coord.Plus(shift)]; !ok {
					areaBorder.Coord = coord
					areaBorder.Dirs |= dir
				}
			}

			if !areaBorder.Coord.Equals(0, 0, 0) {
				areaZone.Borders = append(areaZone.Borders, areaBorder)
			}
		}

		areaZones = append(areaZones, areaZone)
	}

	e.areasZones = areaZones
}
