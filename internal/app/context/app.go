package context

import (
	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/internal/app/byond"
)

func (c *Context) checkShouldClose() {
	if c.tmpShouldClose {
		glfw.GetCurrentContext().SetShouldClose(true)
	}
}

func (c *Context) openEnvironment(file string) {
	byond.NewDme(file)
}
