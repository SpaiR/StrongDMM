package prefs

type Prefs struct {
	Editor      Editor
	Controls    Controls
	Interface   Interface
	Application Application
}

type Interface struct {
	Scale int
	Fps   int
}

type Controls struct {
	AltScrollBehaviour   bool
	QuickEditContextMenu bool
	QuickEditMapPane     bool
}

type Editor struct {
	SaveFormat        string
	CodeEditor        string
	NudgeMode         string
	SanitizeVariables bool
}

type Application struct {
	CheckForUpdates bool
	AutoUpdate      bool
}
