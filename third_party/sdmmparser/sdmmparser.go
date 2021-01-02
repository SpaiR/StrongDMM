package sdmmparser

/*
#cgo CFLAGS: -I./lib
#cgo LDFLAGS: -L./lib -lsdmmparser
#include <stdlib.h>
#include "lib/sdmmparser.h"
*/
import "C"
import (
	"unsafe"
)

func ParseEnvironment(environmentPath string) string {
	nativePath := C.CString(environmentPath)
	defer C.free(unsafe.Pointer(nativePath))

	nativeStr := C.SdmmParseEnvironment(nativePath)
	defer C.SdmmFreeStr(nativeStr)

	return C.GoString(nativeStr)
}
