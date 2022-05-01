package shortcut

import (
	"runtime"
	"strings"

	"github.com/go-gl/glfw/v3.3/glfw"
)

func Combine(keys ...string) string {
	return strings.Join(keys, "+")
}

var isDarwin = runtime.GOOS == "darwin"

func KeyModName() string {
	if isDarwin {
		return "Cmd"
	}
	return "Ctrl"
}

func KeyModLeft() glfw.Key {
	if isDarwin {
		return glfw.KeyLeftSuper
	}
	return glfw.KeyLeftControl
}

func KeyModRight() glfw.Key {
	if isDarwin {
		return glfw.KeyRightSuper
	}
	return glfw.KeyRightControl
}
