package karnickeldev.solar.worldview.orbitsolver.mathkernel;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.worldview.orbitsolver.OrbitLocalFrame;
import karnickeldev.solar.worldview.orbitsolver.OrbitMathKernel;
import org.bouncycastle.util.test.SimpleTest;

/**
 * @author KarnickelDev
 * @since 03.03.2026
 **/
public final class OrbitMathKernelSIMDCritical implements OrbitMathKernel {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private static final DoubleVector ONE = DoubleVector.broadcast(SPECIES, 1.0);

    private static final ThreadLocal<double[]> SCRATCH_X = ThreadLocal.withInitial(() -> new double[SPECIES.length()]);
    private static final ThreadLocal<double[]> SCRATCH_Y = ThreadLocal.withInitial(() -> new double[SPECIES.length()]);

    public void computeRange(OrbitDataSoA soa, OrbitLocalFrame out, long simTimeMicros, int low, int high) {
        int vecLen = SPECIES.length();

        DoubleVector simTimeSeconds = DoubleVector.broadcast(SPECIES, simTimeMicros).mul(1e-6);

        // SIMD vectorized position compute using linear SoA inputs
        int vecEnd = low + SPECIES.loopBound(high - low);

        for (int base = low; base < vecEnd; base += vecLen) {
            DoubleVector vA = DoubleVector.fromArray(SPECIES, soa.a, base);
            DoubleVector vE = DoubleVector.fromArray(SPECIES, soa.e, base);
            DoubleVector vN = DoubleVector.fromArray(SPECIES, soa.meanMotion, base);

            DoubleVector vCosW = DoubleVector.fromArray(SPECIES, soa.cosOmega, base);
            DoubleVector vSinW = DoubleVector.fromArray(SPECIES, soa.sinOmega, base);
            DoubleVector vSqrt = DoubleVector.fromArray(SPECIES, soa.sqrtOneMinusE2, base);

            // compute dt
            DoubleVector vDt = simTimeSeconds.sub(DoubleVector.fromArray(SPECIES, soa.t0Seconds, base));

            // Mean anomaly
            DoubleVector vM = vN.mul(vDt);

            // Initial guess
            DoubleVector vEcc = vM;

            DoubleVector sinE;
            DoubleVector cosE;

            // Newton iterations
            for(int iter = 0; iter < 4; iter++) {
                sinE = vEcc.lanewise(VectorOperators.SIN);
                cosE = vEcc.lanewise(VectorOperators.COS);

                DoubleVector f = vEcc.sub(vE.mul(sinE)).sub(vM);
                DoubleVector fp = ONE.sub(vE.mul(cosE));

                vEcc = vEcc.sub(f.div(fp));
            }

            // compute sin/cos once
            sinE = vEcc.lanewise(VectorOperators.SIN);
            cosE = vEcc.lanewise(VectorOperators.COS);

            // orbital coordinates
            DoubleVector vX = vA.mul(cosE.sub(vE));
            DoubleVector vY = vA.mul(vSqrt.mul(sinE));

            // rotate by omega
            DoubleVector vRotX = vCosW.mul(vX).sub(vSinW.mul(vY));
            DoubleVector vRotY = vSinW.mul(vX).add(vCosW.mul(vY));

            vRotX.intoArray(SCRATCH_X.get(), 0);
            vRotY.intoArray(SCRATCH_Y.get(), 0);

            for(int i = 0; i < vecLen; i++) {
                int idx = base + i;
                int anchor = soa.anchorIds[idx];

                out.localX[anchor] = SCRATCH_X.get()[i];
                out.localY[anchor] = SCRATCH_Y.get()[i];
            }
        }


        // Tail scalar (remainder of entities that don't fill another SIMD lane)
        for (int idx = vecEnd; idx < high; idx++) {
            double a = soa.a[idx];
            double e = soa.e[idx];
            double n = soa.meanMotion[idx];
            double sqrt = soa.sqrtOneMinusE2[idx];

            double cosW = soa.cosOmega[idx];
            double sinW = soa.sinOmega[idx];

            double dt = (simTimeMicros * 1e-6) - soa.t0Seconds[idx];
            double M = n * dt;

            double E = OrbitMathKernelSimple.solveKepler(M, e);

            double cosE = Math.cos(E);
            double sinE = Math.sin(E);

            double x = a * (cosE - e);
            double y = a * sqrt * sinE;

            int anchor = soa.anchorIds[idx];
            out.localX[anchor] = cosW * x - sinW * y;
            out.localY[anchor] = sinW * x + cosW * y;
        }

        double AU = Units.toSU(1, Units.Length.AU);
        double invAU = 1.0 / AU;

        for(int idx = low; idx < high; idx++) {
            int anchor = soa.anchorIds[idx];

            short sx = (short) Math.floor(out.localX[anchor] * invAU);
            short sy = (short) Math.floor(out.localY[anchor] * invAU);

            out.sectorX[anchor] = sx;
            out.sectorY[anchor] = sy;

            out.localX[anchor] -= sx * AU;
            out.localY[anchor] -= sy * AU;
        }
    }

}
