package brush

import (
	"log"

	"github.com/go-gl/gl/v3.3-core/gl"
	"strongdmm/platform"
)

var (
	initialized bool

	attrsList attributesList

	program uint32

	vao uint32
	vbo uint32
	ebo uint32
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
