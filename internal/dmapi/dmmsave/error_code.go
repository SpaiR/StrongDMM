package dmmsave

import "github.com/rs/zerolog/log"

//nolint:errname
type saveErrorCode int

const (
	errRegenerateKeys saveErrorCode = iota
	errKeysLimitExceeded
)

func (s saveErrorCode) Error() string {
	switch s {
	case errRegenerateKeys:
		return "regenerate keys error"
	case errKeysLimitExceeded:
		return "keys limit exceeded error"
	}
	log.Panic().Msg("unknown error code")
	return "" // unreachable
}
