package workspace

import "sdmm/app/command"

type Workspace interface {
	CommandStackId() string
	Name() string
	PreProcess()
	Process()
	WantClose() bool
	Dispose()
	Select(bool)
	IsDoSelect() bool
	HasTooltip() bool
	Tooltip() string
	Border() bool
	SetIdx(idx int)
}

type Base struct {
	Workspace

	isDoSelect bool
	idx        int
}

func (*Base) PreProcess() {
	// do nothing
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

func (b *Base) Idx() int {
	return b.idx
}

func (b *Base) SetIdx(idx int) {
	b.idx = idx
}
