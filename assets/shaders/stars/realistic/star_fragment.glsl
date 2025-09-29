#ifdef GL_ES
precision mediump float;
#endif

#include "../../webgl-noise/noise3D.glsl"
#include "../blackbody.glsl"

varying vec2 v_uv;

uniform float u_temperature;
uniform float u_time;

vec3 starColor(float T, float totalNoise) {
    vec3 bb = blackBody(T);

    // Hotter stars -> more white "washout"
    float whiteness = smoothstep(5000.0, 15000.0, T) * 0.9;

    // Mix towards white
    vec3 col = mix(bb, vec3(1.0), whiteness);

    // Temperature-dependent noise modulation
    float noiseStrength = mix(1.2, 0.7, clamp((T - 2500.0) / 15000.0, 0.0, 1.0));
        col = mix(col * (1.0 - 0.2 * noiseStrength),
        col * (1.0 + 0.3 * noiseStrength),
    totalNoise);

    return col;
}

void main() {
    vec2 uv = (v_uv - vec2(0.5)) * 2;

    float dist = length(uv);
    if (dist > 1.0) discard;

    // normal noise variation
    float detail = mix(10.0, 60.0, clamp((u_temperature - 2500.0) / 15000.0, 0.0, 1.0));
    vec3 pos1 = vec3(uv * 0.15, 1);
    vec3 pos2 = vec3(uv * detail, u_time * 0.2);
    vec3 pos3 = vec3(uv * detail * 2.0, u_time * 0.5);

    float n = 0.5 * (snoise(pos1) + 1) * 0.5;
    n += 0.3 * (snoise(pos2) + 1) * 0.5;
    n += 0.2 * (snoise(pos3) + 1) * 0.5;

    // Sunspots
    float s = 0.1;
    float frequency = 3.0;
    float t1 = snoise(vec3(uv * frequency, 69)) - s;
    float t2 = snoise(vec3((uv + 420.0) * frequency, 69)) - s;
    float ss = (max(t1, 0.0) * max(t2, 0.0)) * 0.9;

    // Accumulate total noise
    float total = n - ss;

    vec3 color = starColor(u_temperature, total);

    // smooth edge
    float alpha = smoothstep(1.0, 0.9, dist);

    // final output
    gl_FragColor = vec4(color, alpha);
}
