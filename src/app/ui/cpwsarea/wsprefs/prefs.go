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

type Prefs map[PrefGroup][]interface{}

func MakePrefs() Prefs {
	return make(map[PrefGroup][]interface{})
}

func (p Prefs) Add(group PrefGroup, pref interface{}) {
	p[group] = append(p[group], pref)
}
