package prefs

import (
	"log"

	"sdmm/app/ui/cpwsarea/wsprefs"
)

type prefPrefab interface {
	make() any
}

type intPrefPrefab struct {
	name  string
	desc  string
	label string
	min   int
	max   int
	value *int
	post  func(int)
}

func (p intPrefPrefab) make() any {
	pref := wsprefs.MakeIntPref()
	pref.Name = p.name
	pref.Desc = p.desc
	pref.Label = p.label
	pref.Min = p.min
	pref.Max = p.max

	pref.FGet = func() int {
		return *p.value
	}
	pref.FSet = func(value int) {
		log.Printf("[app] preferences changing, [%s] to: %d", p.label, value)
		*p.value = value
		if p.post != nil {
			p.post(value)
		}
	}

	return pref
}

type optionPrefPrefab struct {
	name    string
	desc    string
	label   string
	value   *string
	post    func(string)
	options []string
	help    string
}

func (p optionPrefPrefab) make() any {
	pref := wsprefs.MakeOptionPref()
	pref.Name = p.name
	pref.Desc = p.desc
	pref.Label = p.label

	pref.FGet = func() string {
		return *p.value
	}
	pref.FSet = func(value string) {
		log.Printf("[app] preferences changing, [%s] to: %s", p.label, value)
		*p.value = value
		if p.post != nil {
			p.post(value)
		}
	}

	pref.Options = p.options
	pref.Help = p.help

	return pref
}

type boolPrefPrefab struct {
	name  string
	desc  string
	label string
	value *bool
	post  func(bool)
}

func (p boolPrefPrefab) make() any {
	pref := wsprefs.MakeBoolPref()
	pref.Name = p.name
	pref.Desc = p.desc
	pref.Label = p.label

	pref.FGet = func() bool {
		return *p.value
	}
	pref.FSet = func(value bool) {
		log.Printf("[app] preferences changing, [%s] to: %t", p.label, value)
		*p.value = value
		if p.post != nil {
			p.post(value)
		}
	}

	return pref
}
