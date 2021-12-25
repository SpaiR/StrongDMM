package shortcut

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"runtime"
)

var isDarwin = runtime.GOOS == "darwin"

func KeyCmdName() string {
	if isDarwin {
		return "Cmd"
	}
	return "Ctrl"
}

func KeyLeftCmd() glfw.Key {
	if isDarwin {
		return glfw.KeyLeftSuper
	}
	return glfw.KeyLeftControl
}

func KeyRightCmd() glfw.Key {
	if isDarwin {
		return glfw.KeyRightSuper
	}
	return glfw.KeyRightControl
}
