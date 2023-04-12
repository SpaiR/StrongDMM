package wsprefs

type PrefGroup string

const (
	GPEditor      PrefGroup = "Editor"
	GPControls    PrefGroup = "Controls"
	GPInterface   PrefGroup = "Interface"
	GPApplication PrefGroup = "Application"
)

var prefsGroupOrder = []PrefGroup{
	GPEditor,
	GPControls,
	GPInterface,
	GPApplication,
}

type Prefs map[PrefGroup][]any

func MakePrefs() Prefs {
	return make(Prefs)
}

func (p Prefs) Add(group PrefGroup, pref any) {
	p[group] = append(p[group], pref)
}
