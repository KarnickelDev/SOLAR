#version 330 core

in vec2 v_uv;
in vec4 v_color;
in float v_style;

uniform sampler2D u_texture;

uniform float u_pxRange;

out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float screenPxRange() {
    vec2 unitRange = vec2(u_pxRange) / vec2(textureSize(u_texture, 0));
    vec2 screenTexSize = vec2(1.0) / fwidth(v_uv);
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

void main() {
    vec3 v = texture(u_texture, v_uv).rgb;

    bool bold = (int(v_style) & 1) != 0;
    float weight = bold ? (0.5 / u_pxRange) : 0.0;

    float sd = median(v.r, v.g, v.b);
    float distance = sd - 0.5 + weight;
    float screenPxDistance = screenPxRange() * distance;
    float opacity = clamp(screenPxDistance + 0.5, 0.0, 1.0);

    fragColor = vec4(v_color.rgb, v_color.a * opacity);
}
