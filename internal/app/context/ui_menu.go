package context

func (c *Context) DoExit() {
	c.tmpShouldClose = true
}
