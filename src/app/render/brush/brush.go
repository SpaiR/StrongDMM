package brush

import (
	"log"

	"github.com/go-gl/gl/v3.3-core/gl"
	"sdmm/platform"
)

var (
	initialized bool

	attrsList attributesList

	program uint32

	vao uint32
	vbo uint32
	ebo uint32

	uniformLocationTransform int32
	uniformLocationHasTexture int32
)

func TryInit() {
	if !initialized {
		initialized = true

		log.Println("[brush] initializing...")

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

		log.Println("[brush] initialized")
	}
}

func Dispose() {
	log.Println("[brush] disposing...")
	gl.DeleteVertexArrays(1, &vao)
	gl.DeleteBuffers(1, &vbo)
	gl.DeleteBuffers(1, &ebo)
	log.Println("[brush] disposed")
}

func initShader(vertex, fragment string) {
	log.Println("[brush] initializing shader...")
	var err error
	if program, err = platform.NewShaderProgram(vertex, fragment); err != nil {
		log.Fatal("[brush] unable to create shader:", err)
	}

	indices := [2]uint32{}
	uniforms, freeUniforms := gl.Strs("Transform\x00", "HasTexture\x00")
	gl.GetUniformIndices(program, 2, uniforms, &indices[0])
	freeUniforms()
	uniformLocationTransform = int32(indices[0])
	uniformLocationHasTexture = int32(indices[1])

	log.Println("[brush] shader initialized")
}

func initBuffers() {
	log.Println("[brush] initializing buffers...")
	gl.GenVertexArrays(1, &vao)
	gl.GenBuffers(1, &vbo)
	gl.GenBuffers(1, &ebo)
	log.Println("[brush] buffers initialized")
}

func initAttributes() {
	gl.BindVertexArray(vao)
	gl.BindBuffer(gl.ARRAY_BUFFER, vbo)

	var offset int32
	for idx, attr := range attrsList.attrs {
		gl.EnableVertexAttribArray(uint32(idx))
		gl.VertexAttribPointer(uint32(idx), attr.size, attr.xtype, attr.normalized, attrsList.stride, gl.PtrOffset(int(offset)))
		offset += attr.size * attr.xtypeSize
	}

	gl.BindBuffer(gl.ARRAY_BUFFER, 0)
	gl.BindVertexArray(0)
}
