#version 330 core

in vec2 a_position;
in vec2 a_texCoord0;
in vec4 a_color;

out vec2 v_uv;
out vec4 v_color;

uniform mat4 u_projTrans;

void main() {
    v_uv = a_texCoord0;
    v_color = a_color;

    gl_Position = u_projTrans * vec4(a_position, 0.0, 1.0);
}
