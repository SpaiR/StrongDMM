package brush

import (
	"sdmm/internal/platform"

	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/rs/zerolog/log"
)

var (
	initialized bool

	attrsList attributesList

	program uint32

	vao uint32
	vbo uint32
	ebo uint32

	uniformLocationTransform  int32
	uniformLocationHasTexture int32
)

func TryInit() {
	if !initialized {
		initialized = true

		log.Print("initializing...")

		attrsList.addAttribute(attribute{
			size:       2,
			xtype:      gl.FLOAT,
			xtypeSize:  platform.FloatSize,
			normalized: false,
		})
		attrsList.addAttribute(attribute{
			size:       4,
			xtype:      gl.FLOAT,
			xtypeSize:  platform.FloatSize,
			normalized: false,
		})
		attrsList.addAttribute(attribute{
			size:       2,
			xtype:      gl.FLOAT,
			xtypeSize:  platform.FloatSize,
			normalized: false,
		})

		initShader(vertexShader(), fragmentShader())
		initBuffers()
		initAttributes()

		log.Print("initialized")
	}
}

func Dispose() {
	log.Print("disposing...")
	gl.DeleteVertexArrays(1, &vao)
	gl.DeleteBuffers(1, &vbo)
	gl.DeleteBuffers(1, &ebo)
	log.Print("disposed")
}

func initShader(vertex, fragment string) {
	log.Print("initializing shader...")
	var err error
	if program, err = platform.NewShaderProgram(vertex, fragment); err != nil {
		log.Fatal().Msgf("unable to create shader: %v", err)
	}

	uniformIndices := [2]uint32{}
	uniformNames, freeUniformNames := gl.Strs("Transform\x00", "HasTexture\x00")
	gl.GetUniformIndices(program, 2, uniformNames, &uniformIndices[0])
	freeUniformNames()
	uniformLocationTransform = int32(uniformIndices[0])
	uniformLocationHasTexture = int32(uniformIndices[1])

	log.Print("shader initialized")
}

func initBuffers() {
	log.Print("initializing buffers...")
	gl.GenVertexArrays(1, &vao)
	gl.GenBuffers(1, &vbo)
	gl.GenBuffers(1, &ebo)
	log.Print("buffers initialized")
}

func initAttributes() {
	gl.BindVertexArray(vao)
	gl.BindBuffer(gl.ARRAY_BUFFER, vbo)

	var offset int32
	for idx, attr := range attrsList.attrs {
		gl.EnableVertexAttribArray(uint32(idx))
		gl.VertexAttribPointerWithOffset(uint32(idx), attr.size, attr.xtype, attr.normalized, attrsList.stride, uintptr(offset))
		offset += attr.size * attr.xtypeSize
	}

	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
}
