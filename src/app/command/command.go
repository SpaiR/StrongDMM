package command

type Command struct {
	name string

	undo, redo func()
}

func New(name string, undo, redo func()) Command {
	return Command{name, undo, redo}
}

func (c Command) ReadableName() string {
	return c.name
}

func (c Command) Run() Command {
	c.undo()
	return Command{
		name: c.name,
		undo: c.redo,
		redo: c.undo,
	}
}
