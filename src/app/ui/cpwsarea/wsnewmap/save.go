package wsnewmap

import (
	"log"
	"sdmm/app/prefs"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

func (ws *WsNewMap) SaveNewMap(newpath string) bool {
	log.Println("[wsnewmap] saving new map:", ws.CommandStackId())

	// get the prefs
	savePrefs := ws.app.Prefs().Save
	var isTGM bool
	switch savePrefs.Format {
	case prefs.SaveFormatInitial:
		isTGM = false
	case prefs.SaveFormatTGM:
		isTGM = true
	case prefs.SaveFormatDM:
		isTGM = false
	}

	// we assume the data isnt fucked at this point
	var data = &dmmdata.DmmData{
		Filepath:   newpath,
		IsTgm:      isTGM,
		LineBreak:  "\n",
		MaxX:       ws.mapWidth,
		MaxY:       ws.mapHeight,
		MaxZ:       ws.mapZDepth,
		Dictionary: make(dmmdata.DataDictionary),
		Grid:       make(dmmdata.DataGrid),
	}

	// make this use the base types
	dictionaryData := make(dmmdata.Prefabs, 2)

	// pain. copied at dme.go
	// TODO: move newEmpty under dmmap- wait nvm we need dmenv
	baseAreaPath, _ := ws.app.LoadedEnvironment().Objects["/world"].Vars.Value("area")
	baseTurfPath, _ := ws.app.LoadedEnvironment().Objects["/world"].Vars.Value("turf")
	dictionaryData[0] = dmmap.PrefabStorage.Initial(baseAreaPath)
	dictionaryData[1] = dmmap.PrefabStorage.Initial(baseTurfPath)

	data.Dictionary["a"] = dictionaryData

	// now create the grid
	for z := 0; z < data.MaxZ; z++ {
		for y := 0; y < data.MaxY; y++ {
			for x := 0; x < data.MaxX; x++ {
				// uhh (1 < 1) == true apparently
				data.Grid[util.Point{X: x + 1, Y: y + 1, Z: z + 1}] = dmmdata.Key("a")
			}
		}
	}
	data.Save()

	ws.app.CommandStorage().ForceBalance(ws.CommandStackId())
	return true
}
