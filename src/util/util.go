package util

import "github.com/sqweek/dialog"

// Djb2 is hashing method implemented by spec: http://www.cse.yorku.ca/~oz/hash.html
func Djb2(str string) uint64 {
	var hash uint64 = 5381
	for _, c := range str {
		hash = ((hash << 5) + hash) + uint64(c)
	}
	return hash
}

// ShowErrorDialog shows system error dialog to the user.
// Accepts dialog message to show.
func ShowErrorDialog(msg string) {
	ShowErrorDialogV("", msg)
}

// ShowErrorDialogV shows system error dialog to the user.
// Accepts dialog title and message to show.
func ShowErrorDialogV(title, msg string) {
	b := dialog.MsgBuilder{Msg: msg}
	b.Title(title)
	b.Error()
}
