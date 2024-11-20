#version 330 core

// Input attributes
attribute vec4 a_position;   // Position of the vertex
attribute vec2 a_texCoord;   // Texture coordinate

// Pass data to the fragment shader
varying vec2 v_texCoord;     // Pass texture coordinate to fragment shader

uniform mat4 u_projTrans;

void main() {
    v_texCoord = a_texCoord;
    gl_Position = u_projTrans * a_position;
}
