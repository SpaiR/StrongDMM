package command

import "log"

// NullSpaceStackId is for a stack which won't hold any command and will be always empty.
const NullSpaceStackId = "__NULL_SPACE__"

// Storage is used to store application command and handle undo/redo stuff.
type Storage struct {
	currentStackId string
	commandStacks  map[string]*commandStack
}

func NewStorage() *Storage {
	s := &Storage{commandStacks: make(map[string]*commandStack)}
	s.SetStack(NullSpaceStackId)
	return s
}

func (s *Storage) Free() {
	s.commandStacks = make(map[string]*commandStack, len(s.commandStacks))
	s.SetStack(NullSpaceStackId)
	log.Println("[command] storage free")
}

func (s *Storage) SetStack(id string) {
	if s.currentStackId == id {
		return
	}

	log.Println("[command] changing stack to:", id)

	s.currentStackId = id
	if _, ok := s.commandStacks[id]; !ok {
		s.commandStacks[id] = &commandStack{id: id}
		log.Println("[command] created stack:", id)
	}
}

func (s *Storage) DisposeStack(id string) {
	if id == NullSpaceStackId {
		log.Println("[command] skip disposing for:", id)
		return
	}

	log.Println("[command] disposing stack:", id)
	delete(s.commandStacks, id)
	if s.currentStackId == id {
		s.SetStack(NullSpaceStackId)
	}
}

func (s *Storage) Push(command Command) {
	if s.currentStackId == NullSpaceStackId {
		log.Println("[command] skip pushing for:", s.currentStackId)
		return
	}

	if stack, ok := s.commandStacks[s.currentStackId]; ok {
		logStackAction(stack, "push command: "+command.name)
		stack.undo = append(stack.undo, command)
		stack.redo = stack.redo[:0]
		stack.balance++
	} else {
		logNoStackAvailable("push command")
	}
}

func (s *Storage) Undo() {
	s.UndoV(s.currentStackId)
}

func (s *Storage) UndoV(id string) {
	if stack, ok := s.commandStacks[id]; ok {
		logStackAction(stack, "undo")

		if len(stack.undo) == 0 {
			log.Println("[command] unable to undo empty stack")
			return
		}

		s.undo(stack)
	} else {
		logNoStackAvailable("undo")
	}
}

func (s *Storage) undo(stack *commandStack) {
	var command Command
	command, stack.undo = stack.undo[len(stack.undo)-1], stack.undo[:len(stack.undo)-1]
	stack.redo = append(stack.redo, command.Run())
	stack.balance--
}

func (s *Storage) Redo() {
	s.RedoV(s.currentStackId)
}

func (s *Storage) RedoV(id string) {
	if stack, ok := s.commandStacks[id]; ok {
		logStackAction(stack, "redo")

		if len(stack.redo) == 0 {
			log.Println("[command] unable to read empty stack")
			return
		}

		s.redo(stack)
	} else {
		logNoStackAvailable("redo")
	}
}

func (s *Storage) redo(stack *commandStack) {
	var command Command
	command, stack.redo = stack.redo[len(stack.redo)-1], stack.redo[:len(stack.redo)-1]
	stack.undo = append(stack.undo, command.Run())
	stack.balance++
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
	if stack, ok := s.commandStacks[id]; ok {
		return stack.balance != 0 || stack.appliedCommandId() != stack.balanceCommandId
	}
	return false
}

func (s *Storage) ForceBalance(id string) {
	if id == NullSpaceStackId {
		log.Println("[command] skipping force balance for:", id)
		return
	}

	if stack, ok := s.commandStacks[id]; ok {
		logStackAction(stack, "force balance")
		stack.balance = 0
		stack.balanceCommandId = stack.appliedCommandId()
	}
}

func (s *Storage) Balance(id string) {
	if id == NullSpaceStackId {
		log.Println("[command] skipping balance for:", id)
		return
	}

	if stack, ok := s.commandStacks[id]; ok {
		logStackAction(stack, "balance")

		for {
			if stack.balance == 0 {
				break
			} else if stack.balance > 0 {
				s.undo(stack)
			} else if stack.balance < 0 {
				s.redo(stack)
			}
		}
	}
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

	// Field stores a command id at the moment when the stack was forcefully balanced.
	balanceCommandId uint64
}

func (c commandStack) appliedCommandId() uint64 {
	if len(c.undo) > 0 {
		return c.undo[0].id
	}
	return 0
}
