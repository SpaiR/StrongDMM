package slice

// StrPushUnique pushes string at the beginning of the slice.
// If slice contains the string to push, the old one will be removed.
func StrPushUnique(slice []string, str string) []string {
	idx := -1

	for i, s := range slice {
		if s == str {
			idx = i
			break
		}
	}

	if idx != -1 {
		return append([]string{str}, StrRemoveIdxOrd(slice, idx)...)
	} else {
		return append([]string{str}, slice...)
	}
}

// StrRemoveIdx removes element from slice by index without order preserving.
func StrRemoveIdx(slice []string, idx int) []string {
	slice[idx] = slice[len(slice)-1]
	return slice[:len(slice)-1]
}

// StrRemoveIdxOrd removes element from slice by index with order preserving.
func StrRemoveIdxOrd(slice []string, idx int) []string {
	return append(slice[:idx], slice[idx+1:]...)
}
