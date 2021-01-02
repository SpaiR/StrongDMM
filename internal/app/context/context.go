package context

import (
	"github.com/SpaiR/strongdmm/internal/app/service"
	"github.com/SpaiR/strongdmm/internal/app/ui"
	"github.com/SpaiR/strongdmm/internal/app/ui/shortcut"
)

type Context struct {
	tmpShouldClose bool

	uiMenu             *ui.Menu
	serviceEnvironment *service.Environment
}

func NewContext() *Context {
	ctx := Context{}
	ctx.uiMenu = ui.NewMenu(&ctx)
	ctx.serviceEnvironment = service.NewEnvironment(&ctx)
	return &ctx
}

func (c *Context) Process() {
	shortcut.Process()

	c.uiMenu.Process()

	c.postProcess()
	c.dropTmpState()
}

func (c *Context) postProcess() {
	c.checkShouldClose()
}

func (c *Context) dropTmpState() {
	c.tmpShouldClose = false
}
