package prefs

type Prefs struct {
	Interface Interface
	Controls  Controls
}

type Interface struct {
	Scale int
}

type Controls struct {
	AltScrollBehaviour bool
}
