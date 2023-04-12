package wsmap

import (
	"log"

	"sdmm/internal/app/prefs"
	"sdmm/internal/dmapi/dmmsave"
)

func (ws *WsMap) Save() bool {
	log.Println("[wsmap] saving map workspace:", ws.CommandStackId())

	editorPrefs := ws.app.Prefs().Editor

	var saveFormat dmmsave.Format
	switch editorPrefs.SaveFormat {
	case prefs.SaveFormatInitial:
		saveFormat = dmmsave.FormatInitial
	case prefs.SaveFormatTGM:
		saveFormat = dmmsave.FormatTGM
	case prefs.SaveFormatDMM:
		saveFormat = dmmsave.FormatDM
	}

	dmmsave.Save(ws.app.LoadedEnvironment(), ws.paneMap.Dmm(), dmmsave.Config{
		Format:            saveFormat,
		SanitizeVariables: editorPrefs.SanitizeVariables,
	})

	ws.app.CommandStorage().ForceBalance(ws.CommandStackId())
	return true
}
