#version 420 core

layout(location=0) out vec4 color;
layout(location=1) in vec3 outColor;

void main(void) {
	color = vec4(outColor * 2, 1.0);
}
