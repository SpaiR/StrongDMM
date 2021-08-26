package workspace

type Workspace interface {
	Id() string
	Name() string
	Process()
	WantClose() bool
	Dispose()
	Select(bool)
	IsDoSelect() bool
	HasTooltip() bool
	Tooltip() string
	Border() bool
}

type base struct {
	Workspace

	isDoSelect bool
}

func (*base) Id() string {
	return ""
}

func (*base) WantClose() bool {
	return true
}

func (*base) Dispose() {
}

func (b *base) Select(value bool) {
	b.isDoSelect = value
}

func (b *base) IsDoSelect() bool {
	return b.isDoSelect
}

func (b *base) HasTooltip() bool {
	return b.Workspace.Tooltip() != ""
}

func (*base) Tooltip() string {
	return ""
}

func (*base) Border() bool {
	return true
}
