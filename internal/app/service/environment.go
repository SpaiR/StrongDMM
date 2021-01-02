package service

import "github.com/SpaiR/strongdmm/third_party/sdmmparser"

type Environment struct{}

func NewEnvironment() *Environment {
	return &Environment{}
}

func (e *Environment) OpenEnvironment(file string) {
	sdmmparser.ParseEnvironment(file)
}
