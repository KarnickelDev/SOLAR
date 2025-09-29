// Tanner Helland's algorithm: https://tannerhelland.com/2012/09/18/convert-temperature-rgb-algorithm-code.html
// T in Kelvin, range [1000, 40000]
vec3 blackBody(float T) {

    float t = clamp(T, 1000.0, 40000.0) / 100.0;

    float r, g, b;

    // Red
    if (t <= 66.0) {
        r = 1.0;
    } else {
        r = clamp(1.292936186062745 * pow(t - 60.0, -0.1332047592), 0.0, 1.0);
    }

    // Green
    if (t <= 66.0) {
        g = clamp(0.3900815787690196 * log(t) - 0.6318414437886275, 0.0, 1.0);
    } else {
        g = clamp(1.129890860895294 * pow(t - 60.0, -0.0755148492), 0.0, 1.0);
    }

    // Blue
    if (t >= 66.0) {
        b = 1.0;
    } else if (t <= 19.0) {
        b = 0.0;
    } else {
        b = clamp(0.543206789110196 * log(t - 10.0) - 1.19625408914, 0.0, 1.0);
    }

    return vec3(r, g, b);
}
