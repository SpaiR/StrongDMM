package byond

import "strings"

func IsPath(p1, p2 string) bool {
	return strings.HasPrefix(p1, p2)
}
