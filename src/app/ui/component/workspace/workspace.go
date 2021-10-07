package workspace

import "sdmm/app/command"

type Workspace interface {
	CommandStackId() string
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

type Base struct {
	Workspace

	isDoSelect bool
}

func (*Base) CommandStackId() string {
	return command.NullSpaceStackId
}

func (*Base) WantClose() bool {
	return true
}

func (*Base) Dispose() {
}

func (b *Base) Select(value bool) {
	b.isDoSelect = value
}

func (b *Base) IsDoSelect() bool {
	return b.isDoSelect
}

func (b *Base) HasTooltip() bool {
	return b.Workspace.Tooltip() != ""
}

func (*Base) Tooltip() string {
	return ""
}

func (*Base) Border() bool {
	return true
}
