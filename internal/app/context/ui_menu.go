package context

import "github.com/sqweek/dialog"

func (c *Context) DoOpenEnvironment() {
	file, err := dialog.File().Title("Open Environment").Filter("*.dme", "dme").Load()
	if err != nil {
		return // No file to open
	}
	c.openEnvironment(file)
}

func (c *Context) DoExit() {
	c.tmpShouldClose = true
}
