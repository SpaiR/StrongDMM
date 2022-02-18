package selfupdate

import (
	"bytes"
	"fmt"
	"github.com/inconshreveable/go-update"
	"os"
	"path/filepath"
	"runtime"
)

func Update(build []byte) error {
	// When self update applied for Windows it replaces an exe file.
	// The current file will be renamed to the "old" and the new one to the "current".
	// To hide the creation of the "old" file we create a temp holder file.
	var oldSavePath string
	if runtime.GOOS == "windows" {
		f, err := os.Create(filepath.Join(os.TempDir(), "strongdmm-update-holder"))
		if err != nil {
			return fmt.Errorf("unable to create update holder: %v", err)
		}
		f.Close()
		oldSavePath = f.Name()
	}

	if err := update.Apply(bytes.NewReader(build), update.Options{
		OldSavePath: oldSavePath,
	}); err != nil {
		return fmt.Errorf("unable to self-update: %v", err)
	}

	return nil
}
