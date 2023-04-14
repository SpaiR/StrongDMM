package selfupdate

import (
	"bytes"
	"fmt"

	"github.com/minio/selfupdate"
)

func Update(build []byte) error {
	if err := selfupdate.Apply(bytes.NewReader(build), selfupdate.Options{}); err != nil {
		return fmt.Errorf("unable to self-update: %w", err)
	}
	return nil
}
