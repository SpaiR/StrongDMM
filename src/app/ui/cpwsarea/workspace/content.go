package workspace

import (
	"fmt"
	"time"

	"sdmm/app/command"

	"github.com/SpaiR/imgui-go"
)

type content interface {
	Id() string
	Name() string
	Title() string

	Focused() bool
	Closed() bool
	OnFocusChange(focused bool)

	PreProcess()
	Process()
	PostProcess()

	Save() bool
	CommandStackId() string
	Ini() Ini

	Dispose()
}

type Content struct {
	id string

	closed bool
}

func (c *Content) Close() {
	c.closed = true
}

var contentCount uint64

func (c *Content) Id() string {
	if c.id == "" {
		c.id = fmt.Sprint("content_", time.Now().Nanosecond(), "_", contentCount)
		contentCount++
	}
	return c.id
}

func (Content) Focused() bool {
	return imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows)
}

func (c *Content) Closed() bool {
	return c.closed
}

func (Content) OnFocusChange(bool) {
	// do nothing
}

func (Content) PreProcess() {
	// do nothing
}

func (Content) Process() {
	// do nothing
}

func (Content) PostProcess() {
	// do nothing
}

func (Content) Save() bool {
	return false
}

func (Content) CommandStackId() string {
	return command.NullSpaceStackId
}

func (Content) Ini() Ini {
	return Ini{}
}

func (Content) Dispose() {
	// do nothing
}
