#ifdef GL_ES
precision mediump float;
#endif

#include "../webgl-noise/noise3D.glsl"
#include "blackbody.glsl"

varying vec2 v_uv;

uniform float u_temperature;
uniform float u_time;

float coronaFactor(float T) {
    float shaped = pow(smoothstep(1000, 15000, T), 0.36);
    return mix(0.65, 1.2, shaped);
}

void main() {
    vec2 uv = v_uv;
    float r = length(uv) * 4;
    if (r > 4) discard;

    float closeness = 0;

    // Base exponential falloff
    float falloff = exp(0.9-0.9*r);

    // Add radial ray-like noise
    float angle = atan(uv.y, uv.x);
    float rayNoise = snoise(vec3(cos(angle)*2.5, sin(angle)*2.0, u_time*0.3));
    float ray = (0.7 + 0.3 * rayNoise);

    float noisyFalloff = falloff - 0.2*(0.5 + 0.5*snoise(vec3(uv, u_time*0.1)));

    float intensity = 0.6*(falloff * ray) + noisyFalloff;

    // Temperature-driven color (blackbody to bluish-white)
    vec3 bb = blackBody(u_temperature);
    vec3 coronaColor = bb*0.85;

    intensity *= coronaFactor(u_temperature);

    gl_FragColor = vec4(coronaColor * intensity, intensity);
}
