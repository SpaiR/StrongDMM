package service

type action interface {
}

type Environment struct {
	action action
}

func NewEnvironment(action action) *Environment {
	return &Environment{
		action: action,
	}
}

func (e *Environment) OpenEnvironment(file string) {

}
