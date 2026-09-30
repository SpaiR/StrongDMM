package dialog

import (
	"fmt"

	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/imguiext/icon"

	"github.com/SpaiR/imgui-go"
)

type TypeJumpTo struct {
	Title         string
	Information   string
	UndefinedVars []dmmap.UndefinedVar
	OnJump        func(x, y, z int)
}

func (t TypeJumpTo) Name() string {
	return t.Title
}

func (TypeJumpTo) HasCloseButton() bool {
	return true
}

// snowflake dialog for undeclared variables warning when opening a map.
func (t TypeJumpTo) Process() {
	imgui.BeginTable("Undefined Variables", 4)
	imgui.TableSetupColumn("")
	imgui.TableSetupColumn("")
	imgui.TableSetupColumn("")
	imgui.TableSetupColumn("")
	for i, undef := range t.UndefinedVars {
		imgui.TableNextRow()
		imgui.TableSetColumnIndex(0)
		imgui.Text(undef.Path)

		imgui.TableSetColumnIndex(1)
		coords := fmt.Sprintf("(%d, %d, %d)", undef.X, undef.Y, undef.Z)
		imgui.Text(coords)

		imgui.TableSetColumnIndex(2)
		imgui.Text(undef.VarName)

		imgui.TableSetColumnIndex(3)
		buttonLabel := fmt.Sprintf(icon.Search+"##%d", i)
		if imgui.Button(buttonLabel) {
			if t.OnJump != nil {
				t.OnJump(undef.X, undef.Y, undef.Z)
			}
		}
	}
	imgui.EndTable()
}
