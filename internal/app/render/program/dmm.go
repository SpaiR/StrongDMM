package program

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/platform"
	"github.com/go-gl/gl/v3.3-core/gl"
)

type Dmm struct {
	program
}

func NewDmm() *Dmm {
	dmm := &Dmm{}

	dmm.attributes.addAttribute(attribute{
		size:       2,
		xtype:      gl.FLOAT,
		xtypeSize:  platform.FloatSize,
		normalized: false,
	})
	dmm.attributes.addAttribute(attribute{
		size:       4,
		xtype:      gl.FLOAT,
		xtypeSize:  platform.FloatSize,
		normalized: false,
	})
	dmm.attributes.addAttribute(attribute{
		size:       2,
		xtype:      gl.FLOAT,
		xtypeSize:  platform.FloatSize,
		normalized: false,
	})

	log.Println("[program] initializing dmm...")
	dmm.initShader(dmmVertexShader(), dmmFragmentShader())
	dmm.initBuffers()
	dmm.initAttributes()
	log.Println("[program] dmm initialized")

	return dmm
}

// BatchUnit method adds a unit index to the indices batch.
// Every unit in the bucket is a textured rect, so we persist the batch, if texture has changed and batch another rect.
func (d *Dmm) BatchUnit(unitIdx int, texture uint32) {
	if texture != batchTexture {
		batchPersist()
		batchTexture = texture
	}
	d.batchRect(unitIdx)
}

func dmmVertexShader() string {
	return `
#version 330 core

uniform mat4 Transform;

layout (location = 0) in vec2 in_pos;
layout (location = 1) in vec4 in_color;
layout (location = 2) in vec2 in_texture_uv;

out vec2 frag_texture_uv;
out vec4 frag_color;

void main() {
	frag_texture_uv = in_texture_uv;
	frag_color = in_color;
    gl_Position = Transform * vec4(in_pos, 1, 1);
}
` + "\x00"
}

func dmmFragmentShader() string {
	return `
#version 330 core

uniform sampler2D Texture;

in vec2 frag_texture_uv;
in vec4 frag_color;

out vec4 outputColor;

void main() {
    outputColor = frag_color * texture(Texture, frag_texture_uv);
}
` + "\x00"
}
