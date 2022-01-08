package wsprefs

import "math"

type basePref struct {
	Name  string
	Desc  string
	Label string
}

type IntPref struct {
	basePref

	FGet func() int
	FSet func(int)

	Min, Max       int
	Step, StepFast int
}

func NewIntPref() IntPref {
	return IntPref{
		Min:      math.MinInt,
		Max:      math.MaxInt,
		Step:     1,
		StepFast: 10,
	}
}

type BoolPref struct {
	basePref

	FGet func() bool
	FSet func(bool)
}

func NewBoolPref() BoolPref {
	return BoolPref{}
}
