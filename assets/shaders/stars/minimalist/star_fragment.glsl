#ifdef GL_ES
precision mediump float;
#endif

#include "../../webgl-noise/noise3D.glsl"
#include "../blackbody.glsl"

varying vec2 v_uv;
uniform float u_temperature;
uniform float u_time;
uniform float u_pixelation;
uniform float u_edgeSmoothing;
uniform float u_sunSpotSeed;

vec3 posterize(vec3 color, float steps) {
    return round(color * steps) / steps;
}

vec3 starColor(float T, float totalNoise) {
    vec3 bb = blackBody(T);

    // Hotter stars -> more white "washout"
    float whiteness = smoothstep(5000.0, 30000.0, T) * 0.9;

    // Mix towards white
    vec3 col = mix(bb, vec3(1.0), whiteness);

    // Temperature-dependent noise modulation
    float noiseStrength = mix(1.2, 0.7, clamp((T - 2500.0) / 30000.0, 0.0, 1.0));
    col = mix(
        col * (1.0 - 0.2 * noiseStrength),
        col * (1.0 + 0.3 * noiseStrength),
        totalNoise
    );

    return col;
}

void main() {
    vec2 uv = (v_uv - vec2(0.5)) * 2;

    if(u_pixelation != 0) uv = floor(uv / u_pixelation) * u_pixelation;

    float dist = length(uv);
    if (dist > 1.0) discard;

    // normal noise variation
    float detail = mix(10.0, 50.0, clamp((u_temperature - 2500.0) / 30000.0, 0.0, 1.0));
    detail = 10;
    float activeness = 0.1 * detail;
    vec3 pos1 = vec3(uv * 6,  u_time * 0.2);
    vec3 pos2 = vec3(uv * detail * 2.0, u_time * 0.1 * activeness);
    vec3 pos3 = vec3(uv * detail, u_time * 0.05 * activeness);

    float n = 0.3 * (snoise(pos1) + 1) * 0.5;
    n += 0.4 * (snoise(pos2) + 1) * 0.5;
    n += 0.3 * (snoise(pos3) + 1) * 0.5;

    n = (1.5 * n) - 0.4;

    // Sunspots
    float s = 0.05;
    float frequency = 4.0;
    float seed = u_sunSpotSeed;
    float t1 = snoise(vec3(uv * frequency, seed)) + s;
    float t2 = snoise(vec3((uv + 420.0) * frequency, seed)) + s;
    float ss = (max(t1, 0.0) * max(t2, 0.0)) * 2;
    ss *= 1.0 - smoothstep(0.6, 0.7, dist); // radial mask to limit sunspots to star center
    ss = pow(ss, 4) * 0.5; //"circularize" and darken

    // Accumulate total noise
    float total = n - ss;

    vec3 color = starColor(u_temperature, total);

    // smooth edge
    float alpha = smoothstep(1.0, 1.0 - u_edgeSmoothing, dist);

    // final output
    gl_FragColor = vec4(posterize(color, 10), alpha);
}
