package dmmsave

import (
	"log"

	"sdmm/dm/dmmap"
	"sdmm/util"
)

func Save(dmm *dmmap.Dmm) {
	SaveV(dmm, dmm.Path.Absolute)
}

func SaveV(dmm *dmmap.Dmm, path string) {
	log.Println("[dmmsave] save started [" + path + "]...")

	sp, err := create(dmm, path)
	if err != nil {
		log.Println("[dmmsave] unable to start save process")
		util.ShowErrorDialog("Unable to start save process")
		return
	}

	// TODO: Add map sanitizing

	sp.handleReusedKeys()
	if err = sp.handleLocationsWithoutKeys(); err != nil {
		log.Println("[dmmsave] unable to handle locations without keys:", err)
		util.ShowErrorDialog("Unable to save the map: " + err.Error())
		return
	}
	sp.output.Save()

	log.Println("[dmmsave] save finished")
}
