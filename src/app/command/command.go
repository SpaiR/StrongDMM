package command

// Used provide a unique id for every command.
var commandCounter uint64 = 0

type Command struct {
	id   uint64
	name string

	undo, redo func()
}

func Make(name string, undo, redo func()) Command {
	commandCounter++
	return Command{
		id:   commandCounter,
		name: name,
		undo: undo,
		redo: redo,
	}
}

func (c Command) ReadableName() string {
	return c.name
}

func (c Command) Run() Command {
	c.undo()
	return Command{
		id:   c.id,
		name: c.name,
		undo: c.redo,
		redo: c.undo,
	}
}
