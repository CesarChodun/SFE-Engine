#version 420 core

layout(location=0) in vec2 position;
layout(location=1) out vec3 outColor;

layout(set = 0, binding = 0) uniform Time {
    int time;
} myTime;

void main() {
    outColor = vec3(clamp(0.0, 1.0, -position.x), max(0.0, position.x), max(0.0, position.y));
    float factor = myTime.time * 0.1;
    outColor *= factor;
    gl_Position = vec4(position * (max(factor, 0.025)) * 2, 0.0, 1.0);
}
