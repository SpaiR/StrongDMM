//go:build !windows

package extensions

import "os/exec"

func extensionCommand(path string) *exec.Cmd { return exec.Command(path) }
