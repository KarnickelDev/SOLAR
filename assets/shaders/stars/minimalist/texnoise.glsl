#ifdef GL_ES
precision mediump float;
#endif

float fbmNoise(sampler2D tex, vec3 uv) {
    // Wrap UV for tiling
    vec2 uvWrapped = uv.xy * 0.01;

    // Sample once, get R/G/B as three slices
    vec3 n = texture2D(tex, uvWrapped).rgb;

    // "Z position" in the virtual 3D noise
    float z = fract(uv.z * 0.5); // speed of evolution
    float slice = z * 3.0;

    // Determine which two channels to blend
    float res = 0.0;
    if (slice < 1.0) {
        res = mix(n.r, n.g, smoothstep(0.0, 1.0, slice));
    } else if (slice < 2.0) {
        res = mix(n.g, n.b, smoothstep(0.0, 1.0, slice - 1.0));
    } else {
        res = mix(n.b, n.r, smoothstep(0.0, 1.0, slice - 2.0));
    }
    return clamp(res, 0.0, 1.0);
}

float tNoise(sampler2D tex, vec3 pos) {
    float warp = fbmNoise(tex, pos * 2.0 + vec3(12.34, 56.78, 3.14));
    vec3 warpedUV = pos + warp * 0.2;
    return 2.0 * fbmNoise(tex, warpedUV) - 1.0;
}
