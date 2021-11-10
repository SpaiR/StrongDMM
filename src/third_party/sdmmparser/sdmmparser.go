package sdmmparser

/*
#cgo CFLAGS: -I./lib
#cgo LDFLAGS: -L./src/target/release -lsdmmparser
#cgo linux LDFLAGS: -ldl -lm
#include <stdlib.h>
#include "lib/sdmmparser.h"
*/
import "C"
import (
	"encoding/json"
	"fmt"
	"strings"
	"unsafe"
)

type ObjectTreeType struct {
	Path     string
	Vars     []ObjectTreeVar
	Children []ObjectTreeType
}

type ObjectTreeVar struct {
	Name  string
	Value string
}

func ParseEnvironment(environmentPath string) (*ObjectTreeType, error) {
	nativePath := C.CString(environmentPath)
	defer C.free(unsafe.Pointer(nativePath))

	nativeStr := C.SdmmParseEnvironment(nativePath)
	defer C.SdmmFreeStr(nativeStr)

	str := C.GoString(nativeStr)
	if strings.HasPrefix(str, "error") {
		return nil, fmt.Errorf(str)
	}

	var data ObjectTreeType
	if err := json.Unmarshal([]byte(str), &data); err != nil {
		return nil, fmt.Errorf("unable to deserialize environment: %w", err)
	}

	return &data, nil
}

type IconMetadata struct {
	Width, Height int
	States        []*IconState
}

type IconState struct {
	Name         string
	Dirs, Frames int
}

func ParseIconMetadata(iconPath string) (*IconMetadata, error) {
	nativePath := C.CString(iconPath)
	defer C.free(unsafe.Pointer(nativePath))

	nativeStr := C.SdmmParseIconMetadata(nativePath)
	defer C.SdmmFreeStr(nativeStr)

	str := C.GoString(nativeStr)
	if strings.HasPrefix(str, "error") {
		return nil, fmt.Errorf(str)
	}

	var data IconMetadata
	if err := json.Unmarshal([]byte(str), &data); err != nil {
		return nil, fmt.Errorf("unable to deserialize icon metadata: %w", err)
	}

	return &data, nil
}
