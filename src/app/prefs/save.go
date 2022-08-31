package prefs

const (
	SaveFormatInitial = "Initial"
	SaveFormatTGM     = "TGM"
	SaveFormatDMM     = "DMM"

	SaveFormatHelp = `Initial - the map will be saved in the format in which it was loaded
TGM - a custom map format made by TG, helps to make map file more readable and reduce merge conflicts
DMM - a default map format used by the DM map editor
`
)

var SaveFormats = []string{
	SaveFormatInitial,
	SaveFormatTGM,
	SaveFormatDMM,
}

const (
	SaveNudgeModePixel = "pixel_x/pixel_y"
	SaveNudgeModeStep  = "step_x/step_y"
)

var SaveNudgeModes = []string{
	SaveNudgeModePixel,
	SaveNudgeModeStep,
}
