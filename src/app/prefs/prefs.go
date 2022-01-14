package prefs

type Prefs struct {
	Interface Interface
	Controls  Controls
	Save      Save
}

type Interface struct {
	Scale int
}

type Controls struct {
	AltScrollBehaviour bool
}

type Save struct {
	Format    string
	NudgeMode string
}
