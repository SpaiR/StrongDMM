package cpenvironment

import (
	"fmt"
	"os/exec"
	"path/filepath"

	"sdmm/internal/app/prefs"
	"sdmm/internal/app/ui/layout/lnode"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/imguiext/icon"
	w "sdmm/internal/imguiext/widget"
	"sdmm/internal/platform"

	"github.com/SpaiR/imgui-go"
	"github.com/rs/zerolog/log"
	"github.com/skratchdot/open-golang/open"
)

func (e *Environment) showNodeMenu(n *treeNode) {
	if imgui.BeginPopupContextItemV(fmt.Sprint("environment_node_menu_", n.orig.Path), imgui.PopupFlagsMouseButtonRight) {
		w.Layout{
			w.MenuItem("Find on Map", e.doFindOnMap(n)).
				Icon(icon.Search).
				Enabled(e.app.HasActiveMap()),
			w.MenuItem("Copy Type", e.doCopyType(n)).
				Icon(icon.ContentCopy),
			w.MenuItem("Go to Definition", e.doGoToDefinition(n)).
				Icon(icon.FolderOpen),
		}.Build()
		imgui.EndPopup()
	}
}

func (e *Environment) doFindOnMap(n *treeNode) func() {
	return func() {
		prefab := dmmap.PrefabStorage.Initial(n.orig.Path)
		log.Print("do find object on map:", prefab.Path())
		e.app.ShowLayout(lnode.NameSearch, true)
		e.app.DoSearchPrefabByPath(prefab.Path())
	}
}

func (e *Environment) doCopyType(n *treeNode) func() {
	return func() {
		log.Print("do copy type:", n.orig.Path)
		platform.SetClipboard(n.orig.Path)
	}
}

func (e *Environment) doGoToDefinition(n *treeNode) func() {
	return func() {
		prefab := dmmap.PrefabStorage.Initial(n.orig.Path)
		log.Print("do go to definition:", prefab.Path())

		location := prefab.Location()
		path := filepath.FromSlash(e.app.LoadedEnvironment().RootDir + "/" + location.File)
		editorPrefs := e.app.Prefs().Editor
		var command *exec.Cmd

		switch editorPrefs.CodeEditor {
		case prefs.CodeEditorVSC:
			argument := path + ":" + fmt.Sprint(location.Line) + ":" + fmt.Sprint(location.Column)
			command = exec.Command(prefs.CodeEditorVSCActual, "-g", argument)
		case prefs.CodeEditorDM:
			// No line/col support until https://www.byond.com/forum/post/2970625
			command = exec.Command(prefs.CodeEditorDMActual, path)
		case prefs.CodeEditorNPP:
			lineArg := "-n" + fmt.Sprint(location.Line)
			colArg := "-c" + fmt.Sprint(location.Column)
			command = exec.Command(prefs.CodeEditorNPPActual, path, lineArg, colArg)
		case prefs.CodeEditorDefault:
			if err := open.Start(path); err != nil {
				log.Print("unable to open definition file: ", err)
			}
			return
		}

		if err := command.Start(); err != nil {
			log.Print("unable to open definition file: ", err)
		}
	}
}
