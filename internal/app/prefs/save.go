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
	CodeEditorVSC     = "Visual Studio Code"
	CodeEditorDM      = "Dreammaker"
	CodeEditorNPP     = "Notepad++"
	CodeEditorDefault = "Default App"

	CodeEditorVSCActual = "code"
	CodeEditorDMActual  = "dreammaker"
	CodeEditorNPPActual = "notepad++"

	CodeEditorHelp = `These programs must be present in your system PATH to work when using Go to Definition on a prefab.
Default App and currently Dreammaker can only open to the relevant file, not the specific line number.`
)

var CodeEditors = []string{
	CodeEditorVSC,
	CodeEditorDM,
	CodeEditorNPP,
	CodeEditorDefault,
}

const (
	SaveNudgeModePixel    = "pixel_x/pixel_y"
	SaveNudgeModeStep     = "step_x/step_y"
	SaveNudgeModePixelAlt = "pixel_w/pixel_z"
)

var SaveNudgeModes = []string{
	SaveNudgeModePixel,
	SaveNudgeModeStep,
	SaveNudgeModePixelAlt,
}
