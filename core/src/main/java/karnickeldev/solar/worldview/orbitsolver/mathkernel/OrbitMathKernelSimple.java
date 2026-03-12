package karnickeldev.solar.worldview.orbitsolver.mathkernel;

import karnickeldev.solar.physics.Units;
import karnickeldev.solar.worldview.orbitsolver.OrbitLocalFrame;
import karnickeldev.solar.worldview.orbitsolver.OrbitMathKernel;

/**
 * @author KarnickelDev
 * @since 04.03.2026
 **/
public class OrbitMathKernelSimple implements OrbitMathKernel {

    public static double solveKepler(double M, double e) {
        double E = M + e*Math.sin(M) * (1 + e*Math.cos(M)); // good initial guess for e < 0.2 (most orbits)
        for (int i = 0; i < 4; i++) {
            double f = E - e * Math.sin(E) - M;
            double fp = 1 - e * Math.cos(E);
            double d = f / fp;
            E -= d;
            if (Math.abs(d) < 1e-6) break;
        }

        return E;
    }

    @Override
    public void computeRange(OrbitDataSoA soa, OrbitLocalFrame out, long simTimeMicros, int start, int end) {
        double AU = Units.Length.AU.getBaseFactor();
        double invAU = 1.0 / AU;

        for (int i = start; i < end; i++) {
            double a = soa.a[i];
            double e = soa.e[i];

            double n = soa.meanMotion[i];
            double M = n * ((simTimeMicros * 1e-6) - soa.t0Seconds[i]); // micros -> seconds
            double E = OrbitMathKernelSimple.solveKepler(M, e);
            double theta = 2.0 * Math.atan2(Math.sqrt(1 + e) * Math.sin(E * 0.5), Math.sqrt(1 - e) * Math.cos(E * 0.5));

            double r = a * (1 - e * Math.cos(E));
            double ox = r * Math.cos(theta);
            double oy = r * Math.sin(theta);

            double cosW = soa.cosOmega[i];
            double sinW = soa.sinOmega[i];

            double relToParentX = cosW * ox - sinW * oy;
            double relToParentY = sinW * ox + cosW * oy;

            out.sectorX[i] = (short) Math.floor(relToParentX * invAU);
            out.sectorY[i] = (short) Math.floor(relToParentY * invAU);

            out.localX[i] = relToParentX - out.sectorX[i] * AU;
            out.localY[i] = relToParentY - out.sectorY[i] * AU;
        }
    }
}
