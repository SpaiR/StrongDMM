package dmmsave

import "log"

type saveErrorCode int

const (
	errorRegenerateKeys saveErrorCode = iota
	errorKeysLimitExceeded
)

func (s saveErrorCode) Error() string {
	switch s {
	case errorRegenerateKeys:
		return "regenerate keys error"
	case errorKeysLimitExceeded:
		return "keys limit exceeded error"
	}
	log.Panic("[dmmsave] unknown error code!")
	return "" // unreachable
}
