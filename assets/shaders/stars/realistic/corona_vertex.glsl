attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec2 v_uv;

void main() {
    v_uv = (2.0 * a_texCoord0) - 1.0; // [-1;1]
    gl_Position = u_projTrans * a_position;
}
