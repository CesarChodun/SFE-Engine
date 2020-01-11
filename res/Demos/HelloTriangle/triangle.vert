#version 420 core

layout(location=0) in vec2 position;
layout(location=1) out vec3 outColor;

void main() {
   // outColor = vec3(max(0.0, -position.x), max(0.0, position.x), max(0.0, position.y));
    outColor = vec3(clamp(0.0, 1.0, -position.x), max(0.0, position.x), max(0.0, position.y));
    gl_Position = vec4(position, 0.0, 1.0);
}
