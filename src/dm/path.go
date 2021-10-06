package dm

import "strings"

func IsPath(p1, p2 string) bool {
	return strings.HasPrefix(p1, p2)
}

// PathWeight let us sort the content by the path weight. Basically: /obj->/turf->/area
func PathWeight(p string) int {
	if IsPath(p, "/area") {
		return 3
	}
	if IsPath(p, "/turf") {
		return 2
	}
	return 1
}
