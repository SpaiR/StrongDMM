package selfupdate

import (
	"bytes"
	"fmt"

	"github.com/inconshreveable/go-update"
)

func Update(build []byte) error {
	if err := update.Apply(bytes.NewReader(build), update.Options{}); err != nil {
		return fmt.Errorf("unable to self-update: %v", err)
	}
	return nil
}
