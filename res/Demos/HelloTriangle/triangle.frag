#version 420 core

layout(location=0) out vec4 color;
layout(location=1) in vec3 outColor;

void main(void) {
   // vec3 base = vec3(1, 1, 1);
	 // color = vec4(outColor.x*2, outColor.z*2, outColor.y*2, 1.0);
	//  color = vec4(outColor.z*2, outColor.y*2, outColor.x*2, 1.0);
  color = vec4(outColor*2, 1.0);
}
