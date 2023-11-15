package psettings

import (
	"fmt"
	"image/png"
	"os"
	"time"

	"sdmm/internal/app/render/bucket/level/chunk/unit"
	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap/canvas"
	"sdmm/internal/app/ui/cpwsarea/wsmap/tools"
	appdialog "sdmm/internal/app/ui/dialog"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/imguiext"
	"sdmm/internal/imguiext/icon"
	"sdmm/internal/imguiext/style"
	w "sdmm/internal/imguiext/widget"
	"sdmm/internal/util"

	"github.com/SpaiR/imgui-go"
	"github.com/rs/zerolog/log"
	"github.com/sqweek/dialog"
	"golang.design/x/clipboard"
)

type sessionScreenshot struct {
	saving          bool
	inSelectionMode bool
	toClipboardMode bool
}

func (p *Panel) showScreenshot() {
	if imgui.CollapsingHeader("Screenshot") {
		if imgui.Button(icon.FolderOpen) {
			p.selectScreenshotDir()
		}

		imguiext.SetItemHoveredTooltip("Screenshot Folder")

		imgui.SameLine()

		imgui.SetNextItemWidth(-1)
		imgui.InputText("##screenshot_dir", &cfg.ScreenshotDir)

		if imgui.Checkbox("Screenshot in Selection", &p.sessionScreenshot.inSelectionMode) {
			tools.SetSelected(tools.TNGrab)
		}
		
		imgui.Checkbox("To Clipboard", &p.sessionScreenshot.toClipboardMode)

		var createBtnLabel string
		if p.sessionScreenshot.saving {
			createBtnLabel = "Creating" + []string{".", "..", "...", "...."}[int(imgui.Time()/.25)&3] + "###create"
		} else {
			createBtnLabel = icon.Save + "Create###create"
		}

		w.Layout{
			w.Disabled(p.sessionScreenshot.saving,
				w.Button(createBtnLabel, p.createScreenshot).
					Size(imgui.Vec2{X: -1}).
					Style(style.ButtonGreen{}),
			),
		}.Build()
	}
}

func (p *Panel) createScreenshot() {
	p.sessionScreenshot.saving = true
	selectedTool := tools.Selected()
	grabCurrentlySelected := selectedTool.Name() == tools.TNGrab
	boundX, boundY := float32(0), float32(0)
	var width, height int
	if p.sessionScreenshot.inSelectionMode {
		if !grabCurrentlySelected || !selectedTool.(*tools.ToolGrab).HasSelectedArea() {
			appdialog.Open(appdialog.TypeInformation{
				Title:       "Nothing selected!",
				Information: "Screenshot in Selection is on, but you have nothing selected. Use the grab tool!",
			})
			p.sessionScreenshot.saving = false
			return
		} else {
			bounds := selectedTool.(*tools.ToolGrab).Bounds() //get grab tool bounds so we can calculate boundX and boundY
			width, height = (int(bounds.X2-bounds.X1)+1)*dmmap.WorldIconSize, (int(bounds.Y2-bounds.Y1)+1)*dmmap.WorldIconSize
			boundX = -float32((int(bounds.X1) - 1) * dmmap.WorldIconSize) //now change bounds so we can use them in Translate
			boundY = -float32((int(bounds.Y1) - 1) * dmmap.WorldIconSize)
		}
	} else {
		width, height = p.editor.Dmm().MaxX*dmmap.WorldIconSize, p.editor.Dmm().MaxY*dmmap.WorldIconSize
	}

	c := canvas.New()
	c.ClearColor = canvas.Color{} // Empty clear color with no alpha
	c.Render().Camera.Level = p.editor.ActiveLevel()
	c.Render().Camera.Translate(boundX, boundY)
	c.Render().SetUnitProcessor(p)
	for level := 1; level <= p.editor.ActiveLevel(); level++ {
		c.Render().UpdateBucket(p.editor.Dmm(), level) // Prepare for render all available levels
	}
	c.Process(imgui.Vec2{X: float32(width), Y: float32(height)})
	c.Dispose()

	var pixels = c.ReadPixels()

	go func() {
		if err := p.saveScreenshot(pixels, width, height); err != nil {
			appdialog.Open(appdialog.TypeInformation{
				Title:       "Error: Screenshot Creation",
				Information: fmt.Sprint("Unable to create screenshot:", err),
			})
		}
		p.sessionScreenshot.saving = false
	}()
}

func (p *Panel) selectScreenshotDir() {
	if dir, err := dialog.
		Directory().
		Title("Screenshot Directory").
		SetStartDir(cfg.ScreenshotDir).
		Browse(); err == nil {
		log.Print("screenshot directory selected:", dir)
		cfg.ScreenshotDir = dir
	}
}

func (p *Panel) ProcessUnit(u unit.Unit) bool {
	return p.app.PathsFilter().IsVisiblePath(u.Instance().Prefab().Path())
}

func (p *Panel) saveScreenshot(pixels []byte, w, h int) error {
	if err := os.MkdirAll(cfg.ScreenshotDir, os.ModeDir); err != nil {
		log.Print("unable to create screenshot directory:", err)
		return err
	}

	out, _ := os.Create(cfg.ScreenshotDir + "/" + time.Now().Format(util.TimeFormat) + ".png")
	if p.sessionScreenshot.toClipboardMode {
		defer fileToClipboardAndDelete(out.Name())
	}
	defer out.Close()

	return png.Encode(out, util.PixelsToRGBA(pixels, w, h))
}

func fileToClipboardAndDelete(filename string) error {
	if err := clipboard.Init(); err != nil {
		log.Print("unable to init clipboard:", err)
		return err
	}
	contents, err := os.ReadFile(filename)
	if err != nil {
		log.Print("unable to read resulting screenshot file:", err)
		return err
	}
	clipboard.Write(clipboard.FmtImage, contents)
	return os.Remove(filename)
}
