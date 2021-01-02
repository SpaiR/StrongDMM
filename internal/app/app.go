package app

import (
	"github.com/SpaiR/strongdmm/internal/app/context"
	"github.com/SpaiR/strongdmm/internal/app/window"
)

const TITLE = "StrongDMM"

func Start() {
	ctx := context.NewContext()

	window.ShowAndRun(TITLE, func() {
		ctx.Process()
	})
}
