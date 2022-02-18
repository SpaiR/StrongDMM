package window

import (
	"log"
	"os"
	"os/exec"
	"runtime"
	"syscall"
)

// Path to the current exe file. Required to do a correct restart after self update applied.
var selfExecutableName string

func init() {
	executableName, err := os.Executable()
	if err != nil {
		panic("unable to get executable name: " + err.Error())
	}
	selfExecutableName = executableName
}

func Restart() {
	if err := restartSelf(); err != nil {
		log.Println("[window], unable to restart gracefully:", err)
		panic("unable to restart gracefully: " + err.Error())
	}
}

func restartSelf() error {
	args := os.Args
	env := os.Environ()

	// Windows requires custom restart logic.
	if runtime.GOOS == "windows" {
		cmd := exec.Command(selfExecutableName, args[1:]...)
		cmd.Stdout = os.Stdout
		cmd.Stderr = os.Stderr
		cmd.Stdin = os.Stdin
		cmd.Env = env
		err := cmd.Run()
		if err == nil {
			os.Exit(0)
		}
		return err
	}

	return syscall.Exec(selfExecutableName, args, env)
}
