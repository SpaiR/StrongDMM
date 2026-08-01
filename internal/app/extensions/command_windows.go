//go:build windows

package extensions

import (
	"os/exec"
	"syscall"
)

func extensionCommand(path string) *exec.Cmd {
	command := exec.Command(path)
	command.SysProcAttr = &syscall.SysProcAttr{HideWindow: true}
	return command
}
