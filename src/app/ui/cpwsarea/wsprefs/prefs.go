package wsprefs

type PrefGroup string

const (
	GPInterface PrefGroup = "Interface Options"
	GPControls  PrefGroup = "Controls Options"
	GPSave      PrefGroup = "Save Options"
)

var prefsGroupOrder = []PrefGroup{
	GPInterface,
	GPControls,
	GPSave,
}

type Prefs map[PrefGroup][]interface{}

func MakePrefs() Prefs {
	return make(map[PrefGroup][]interface{})
}

func (p Prefs) Add(group PrefGroup, pref interface{}) {
	p[group] = append(p[group], pref)
}
