package psettings

import (
	"fmt"
	"github.com/SpaiR/imgui-go"
	"log"
	"math"
	"sdmm/imguiext"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"
)

const (
	possibleMaxX = math.MaxInt
	possibleMaxY = math.MaxInt
	possibleMaxZ = math.MaxInt
)

type sessionMapSize struct {
	maxX, maxY, maxZ int32
}

func (s sessionMapSize) String() string {
	return fmt.Sprintf("maxX: %d, maxY: %d, maxZ: %d", s.maxX, s.maxY, s.maxZ)
}

func (p *Panel) DropSessionMapSize() {
	p.sessionMapSize = nil
}

func (p *Panel) showMapSize() {
	if imgui.CollapsingHeader("Map Size") {
		if p.sessionMapSize == nil {
			p.sessionMapSize = &sessionMapSize{
				maxX: int32(p.editor.Dmm().MaxX),
				maxY: int32(p.editor.Dmm().MaxY),
				maxZ: int32(p.editor.Dmm().MaxZ),
			}
		}

		imgui.AlignTextToFramePadding()
		imgui.Text("X")
		imgui.SameLine()
		imgui.SetNextItemWidth(-1)
		imguiext.InputIntClamp("##max_x", &p.sessionMapSize.maxX, 1, possibleMaxX, 1, 10)

		imgui.AlignTextToFramePadding()
		imgui.Text("Y")
		imgui.SameLine()
		imgui.SetNextItemWidth(-1)
		imguiext.InputIntClamp("##max_y", &p.sessionMapSize.maxY, 1, possibleMaxY, 1, 10)

		imgui.AlignTextToFramePadding()
		imgui.Text("Z")
		imgui.SameLine()
		imgui.SetNextItemWidth(-1)
		imguiext.InputIntClamp("##max_z", &p.sessionMapSize.maxZ, 1, possibleMaxZ, 1, 10)

		imgui.Separator()

		w.Button("Set", p.doSetMapSize).
			Style(style.ButtonGreen{}).
			Build()
	} else {
		p.sessionMapSize = nil
	}
}

func (p *Panel) doSetMapSize() {
	log.Printf("[psettings] do set map size [%s]: %v", p.editor.Dmm().Name, p.sessionMapSize)
	oldMaxX, oldMaxY, oldMaxZ := p.editor.Dmm().MaxX, p.editor.Dmm().MaxY, p.editor.Dmm().MaxZ
	p.editor.Dmm().SetMapSize(int(p.sessionMapSize.maxX), int(p.sessionMapSize.maxY), int(p.sessionMapSize.maxZ))
	p.editor.CommitMapSizeChange(oldMaxX, oldMaxY, oldMaxZ)
	p.sessionMapSize = nil
}
