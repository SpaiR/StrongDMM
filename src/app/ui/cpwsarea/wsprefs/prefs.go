package wsprefs

type PrefGroup string

const (
	GPInterface PrefGroup = "Interface"
	GPControls  PrefGroup = "Controls"
	GPEditor    PrefGroup = "Editor"
)

var prefsGroupOrder = []PrefGroup{
	GPInterface,
	GPControls,
	GPEditor,
}

type Prefs map[PrefGroup][]interface{}

func MakePrefs() Prefs {
	return make(map[PrefGroup][]interface{})
}

func (p Prefs) Add(group PrefGroup, pref interface{}) {
	p[group] = append(p[group], pref)
}
