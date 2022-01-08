package prefs

const (
	SaveFormatInitial = "Initial"
	SaveFormatTGM     = "TGM"
	SaveFormatDM      = "DM"

	SaveFormatHelp = `Initial - the map will be saved in the format in which it was loaded
TGM - a custom map format made by TG, helps to make map file more readable and reduce merge conflicts
DM - a default map format used by the DM map editor
`
)

var SaveFormats = []string{
	SaveFormatInitial,
	SaveFormatTGM,
	SaveFormatDM,
}
