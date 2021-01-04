package slice

func PushUniqueString(slice []string, str string) []string {
	idx := -1

	for i, s := range slice {
		if s == str {
			idx = i
			break
		}
	}

	if idx != -1 {
		return append([]string{str}, RemoveString(slice, idx)...)
	} else {
		return append([]string{str}, slice...)
	}
}

func RemoveString(slice []string, idx int) []string {
	slice[idx] = slice[len(slice)-1]
	return slice[:len(slice)-1]
}
