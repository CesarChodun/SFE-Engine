#version 420 core

layout(location=0) in vec3 position;
layout(location=1) out vec3 outColor;

layout(set = 0, binding = 0) uniform mMX4{
	mat4 transform;
} model;

layout(set = 0, binding = 1) uniform cMX4{
	mat4 transform;
} camera;

void main() {
    outColor = vec3(clamp(0.0, 1.0, -position.x), max(0.0, position.x), max(0.0, position.y));

    mat4 composite = camera.transform * model.transform;
    gl_Position = (composite * vec4(position, 1.0));
    gl_Position = vec4(-gl_Position.x, -gl_Position.y, gl_Position.zw);
}
