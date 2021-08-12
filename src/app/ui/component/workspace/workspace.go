package workspace

type Workspace interface {
	Name() string
	Process()
	WantClose() bool
	Dispose()
	Select(bool)
	IsSelect() bool
	HasTooltip() bool
	Tooltip() string
	Border() bool
}

type base struct {
	Workspace

	isSelect bool
}

func (*base) WantClose() bool {
	return true
}

func (*base) Dispose() {
}

func (b *base) Select(value bool) {
	b.isSelect = value
}

func (b *base) IsSelect() bool {
	return b.isSelect
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
