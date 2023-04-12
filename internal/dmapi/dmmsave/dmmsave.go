package dmmsave

import (
	"log"

	"sdmm/internal/dmapi/dmenv"

	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/util"
)

func Save(dme *dmenv.Dme, dmm *dmmap.Dmm, cfg Config) {
	SaveV(dme, dmm, dmm.Path.Absolute, cfg)
}

func SaveV(dme *dmenv.Dme, dmm *dmmap.Dmm, path string, cfg Config) {
	log.Printf("[dmmsave] save started [%s]...", path)

	sp, err := makeSaveProcess(cfg, dme, dmm, path)
	if err != nil {
		log.Println("[dmmsave] unable to start save process")
		util.ShowErrorDialog("Unable to start save process")
		return
	}

	if cfg.SanitizeVariables {
		sp.sanitizeVariables()
	}

	sp.handleReusedKeys()
	if err = sp.handleLocationsWithoutKeys(); err != nil {
		log.Println("[dmmsave] unable to handle locations without keys:", err)
		util.ShowErrorDialog("Unable to save the map: " + err.Error())
		return
	}
	sp.output.Save()

	log.Println("[dmmsave] save finished")
}
