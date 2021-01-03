package byond

type DmeItem struct {
	env      *Dme
	Type     string
	Vars     map[string]*string
	Children []string
}
