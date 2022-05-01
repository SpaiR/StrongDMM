package wsnewmap

import (
	"log"

	"sdmm/app/prefs"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

func (ws *WsNewMap) save(newPath string) {
	log.Println("[wsnewmap] saving new map:", newPath)

	// we assume the data is OK at this point
	var data = &dmmdata.DmmData{
		Filepath:   newPath,
		IsTgm:      ws.format == prefs.SaveFormatTGM,
		LineBreak:  "\n",
		MaxX:       ws.mapWidth,
		MaxY:       ws.mapHeight,
		MaxZ:       ws.mapZDepth,
		Dictionary: make(dmmdata.DataDictionary),
		Grid:       make(dmmdata.DataGrid),
	}

	data.Dictionary["a"] = dmmdata.Prefabs{
		dmmap.BaseArea,
		dmmap.BaseTurf,
	}

	for z := 1; z <= data.MaxZ; z++ {
		for y := 1; y <= data.MaxY; y++ {
			for x := 1; x <= data.MaxX; x++ {
				data.Grid[util.Point{X: x, Y: y, Z: z}] = "a"
			}
		}
	}

	data.Save()
}
