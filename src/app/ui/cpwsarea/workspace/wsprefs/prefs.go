package wsprefs

type PrefGroup string

const (
	GPInterface PrefGroup = "Interface Options"
)

var prefsGroupOrder = []PrefGroup{GPInterface}

type Prefs map[PrefGroup][]interface{}

func MakePrefs() Prefs {
	return make(map[PrefGroup][]interface{})
}

func (p Prefs) Add(group PrefGroup, pref interface{}) {
	p[group] = append(p[group], pref)
}
