package stbi

// #cgo LDFLAGS: -lm
// #define STB_IMAGE_IMPLEMENTATION
// #define STBI_FAILURE_USERMSG
// #include "lib/stb_image.h"
import "C"
import (
	"errors"
	"image"
	"unsafe"
)

// Load wraps stbi_load to decode an image into an RGBA pixel struct.
func Load(path string) (*image.RGBA, error) {
	cPath := C.CString(path)
	defer C.free(unsafe.Pointer(cPath))

	var x, y C.int
	data := C.stbi_load(cPath, &x, &y, nil, 4)
	if data == nil {
		msg := C.GoString(C.stbi_failure_reason())
		return nil, errors.New(msg)
	}
	defer C.stbi_image_free(unsafe.Pointer(data))

	return &image.RGBA{
		Pix:    C.GoBytes(unsafe.Pointer(data), y*x*4),
		Stride: 4,
		Rect:   image.Rect(0, 0, int(x), int(y)),
	}, nil
}
