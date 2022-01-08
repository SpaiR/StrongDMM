package wsmap

import (
	"log"
	"sdmm/app/prefs"
	"sdmm/dmapi/dmmsave"
)

func (ws *WsMap) Save() {
	log.Println("[wsmap] saving map workspace:", ws.CommandStackId())

	savePrefs := ws.app.Prefs().Save

	var saveFormat dmmsave.Format
	switch savePrefs.Format {
	case prefs.SaveFormatInitial:
		saveFormat = dmmsave.FormatInitial
	case prefs.SaveFormatTGM:
		saveFormat = dmmsave.FormatTGM
	case prefs.SaveFormatDM:
		saveFormat = dmmsave.FormatDM
	}

	dmmsave.Save(ws.paneMap.Dmm(), dmmsave.Config{
		Format: saveFormat,
	})

	ws.app.CommandStorage().ForceBalance(ws.CommandStackId())
}
