package command

import "log"

// Storage is used to store application command and handle undo/redo stuff.
type Storage struct {
	currentStackId string
	commandStacks  map[string]*commandStack
}

func NewStorage() *Storage {
	return &Storage{
		commandStacks: make(map[string]*commandStack),
	}
}

func (s *Storage) SetStack(id string) {
	if s.currentStackId == id {
		return
	}

	log.Println("[command] changing stack to:", id)

	s.currentStackId = id
	if _, ok := s.commandStacks[id]; !ok {
		s.commandStacks[id] = &commandStack{id: id}
	}
}

func (s *Storage) Push(command Command) {
	if stack, ok := s.commandStacks[s.currentStackId]; ok {
		logStackAction(stack, "push command")
		stack.undo = append(stack.undo, command)
		stack.redo = stack.redo[:0]
		stack.balance++
	} else {
		logNoStackAvailable("push command")
	}
}

func (s *Storage) Undo() {
	if stack, ok := s.commandStacks[s.currentStackId]; ok {
		logStackAction(stack, "undo")

		var command Command
		command, stack.undo = stack.undo[len(stack.undo)-1], stack.undo[:len(stack.undo)-1]
		stack.redo = append(stack.redo, command.Run())
		stack.balance--
	} else {
		logNoStackAvailable("undo")
	}
}

func (s *Storage) Redo() {
	if stack, ok := s.commandStacks[s.currentStackId]; ok {
		logStackAction(stack, "redo")

		var command Command
		command, stack.redo = stack.redo[len(stack.redo)-1], stack.redo[:len(stack.redo)-1]
		stack.undo = append(stack.undo, command.Run())
		stack.balance++
	} else {
		logNoStackAvailable("redo")
	}
}

func (s *Storage) HasUndo() bool {
	return s.HasUndoV(s.currentStackId)
}

func (s *Storage) HasUndoV(id string) bool {
	if stack, ok := s.commandStacks[id]; ok {
		return len(stack.undo) > 0
	}
	return false
}

func (s *Storage) HasRedo() bool {
	return s.HasRedoV(s.currentStackId)
}

func (s *Storage) HasRedoV(id string) bool {
	if stack, ok := s.commandStacks[id]; ok {
		return len(stack.redo) > 0
	}
	return false
}

func (s *Storage) IsModified(id string) bool {
	return s.HasUndoV(id)
}

func logNoStackAvailable(action string) {
	log.Println("[command] invalid action, no stack available at the moment:", action)
}

func logStackAction(stack *commandStack, action string) {
	log.Printf("[command] stack action [%s] on [%s]", action, stack.id)
}

type commandStack struct {
	id      string
	balance int
	undo    []Command
	redo    []Command
}
