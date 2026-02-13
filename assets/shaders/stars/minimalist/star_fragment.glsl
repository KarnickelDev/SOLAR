#ifdef GL_ES
precision mediump float;
#endif

#include "../../webgl-noise/noise3D.glsl"
#include "texnoise.glsl"

varying vec2 v_uv;
uniform float u_temperature;
uniform float u_time;
uniform float u_pixelation;
uniform float u_edgeSmoothing;
uniform float u_sunSpotSeed;

uniform sampler2D u_blackBodyTex;
uniform sampler2D u_noiseTex;

vec3 blackBody(float T) {
    float t = clamp((T - 1000.0) / (40000.0 - 1000.0), 0.0, 1.0);
    return texture2D(u_blackBodyTex, vec2(t, 0.5)).rgb;
}

vec3 posterize(vec3 color, float steps) {
    return floor(color * steps) / steps;
}

vec3 starColor(float T, float totalNoise) {
    vec3 bb = blackBody(T);

    // Hotter stars -> more white "washout"
    float whiteness = smoothstep(5000.0, 30000.0, T) * 0.8;

    // Mix towards white
    vec3 col = mix(bb, vec3(1.0), whiteness);

    // Temperature-dependent noise modulation
    //float noiseStrength = mix(1, -1, clamp((T - 2500.0) / 30000.0, 0.0, 1.0));
    col = col + vec3(0.3 * totalNoise);

    return col;
}

float noise(vec3 pos) {
    return tNoise(u_noiseTex, pos);
}

void main() {
    vec2 uv = (v_uv - vec2(0.5)) * 2.0;

    if(u_pixelation != 0.0) uv = floor(uv / u_pixelation) * u_pixelation;

    float dist = length(uv) * 1.0;
    if (dist > 1.0) discard;

    // normal noise variation
    float detail = mix(7.0, 15.0, clamp((u_temperature - 2500.0) / 30000.0, 0.0, 1.0));
    detail = 7.0;
    float activeness = 0.1 * detail;
    vec3 pos1 = vec3(uv * 50.0,  u_time * 0.2);
    vec3 pos2 = vec3(uv * detail * 2.0, u_time * 0.1 * activeness);
    vec3 pos3 = vec3(uv * detail, u_time * 0.05 * activeness);

    float n = 0.3 * (noise(pos1) + 1.0) * 0.5;
    n += 0.5 * (snoise(pos2) + 1.0) * 0.5;
    n += 0.2 * (noise(pos3) + 1.0) * 0.5;

    n = (1.9 * n) - 0.9;

    // Sunspots
    float ss = 0.0;
    //float s = 0.05;
    //float frequency = 4.0;
    //float seed = u_sunSpotSeed;
    //float t1 = noise(vec3(uv * frequency, seed)) + s;
    //float t2 = noise(vec3((uv + 420.0) * frequency, seed)) + s;
    //ss = (max(t1, 0.0) * max(t2, 0.0)) * 2.0;
    //ss *= 1.0 - smoothstep(0.6, 0.7, dist); // radial mask to limit sunspots to star center
    //ss = pow(ss, 4.0) * 0.5; //"circularize" and darken

    // Accumulate total noise
    float total = n - ss;

    vec3 color = starColor(u_temperature, total);

    // smooth edge
    float d = dist - 1.0;
    // Screen-space AA width
    float aa = fwidth(dist);
    // 1 inside disk, smoothly falls to 0 just outside
    float edgeAlpha = 1.0 - smoothstep(-aa, aa, d);

    float alpha = min(edgeAlpha, 0.7);

    vec3 corona = blackBody(u_temperature) * 1.1;
    color = mix(corona, color, alpha);

    // limb darkening
    float mu = sqrt(clamp(1.0 - dist*dist, 0.0, 1.0));
    float limb = mix(0.85, 1.0, mu);
    color *= limb;

    gl_FragColor = vec4(posterize(color, 10.0) * edgeAlpha, edgeAlpha);
}
