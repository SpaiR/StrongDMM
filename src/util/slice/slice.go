package slice

// StrContains returns true if the slice contains the provided string.
func StrContains(slice []string, str string) bool {
	return StrIndexOf(slice, str) != -1
}

// StrPushUnique pushes string at the beginning of the slice.
// If slice contains the string to push, the old one will be removed.
func StrPushUnique(slice []string, str string) []string {
	if idx := StrIndexOf(slice, str); idx != -1 {
		return append([]string{str}, StrRemoveIdx(slice, idx)...)
	} else {
		return append([]string{str}, slice...)
	}
}

// StrIndexOf return the index of the provided string in the slice or -1.
func StrIndexOf(slice []string, str string) int {
	for idx, s := range slice {
		if s == str {
			return idx
		}
	}
	return -1
}

// StrRemoveIdx removes an element from the slice by the index with order preserving.
func StrRemoveIdx(slice []string, idx int) []string {
	return append(slice[:idx], slice[idx+1:]...)
}

// StrRemove removes an element from the slice.
func StrRemove(slice []string, str string) []string {
	return StrRemoveIdx(slice, StrIndexOf(slice, str))
}
