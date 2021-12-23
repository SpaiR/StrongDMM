package workspace

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
)

type Workspace interface {
	CommandStackId() string
	Name() string
	NameReadable() string
	PreProcess()
	Process()
	ShowContent()
	WantClose() bool
	Dispose()
	Select(bool)
	IsDoSelect() bool
	Border() bool
	SetIdx(idx int)
	Focused() bool
	OnActivate()
	OnDeactivate()
}

type Base struct {
	Workspace

	isDoSelect bool
	idx        int

	focused bool
}

func (*Base) PreProcess() {
	// do nothing
}

func (b *Base) Process() {
	b.focused = imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows)
	b.Workspace.ShowContent()
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

func (*Base) Border() bool {
	return true
}

func (b *Base) Idx() int {
	return b.idx
}

func (b *Base) SetIdx(idx int) {
	b.idx = idx
}

func (b *Base) Focused() bool {
	return b.focused
}

func (*Base) OnActivate() {
}

func (*Base) OnDeactivate() {
}
