package karnickeldev.solar.worldview.orbitsolver.mathkernel;

import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.worldview.orbitsolver.OrbitSolveInput;

import java.util.Arrays;

/**
 * @author KarnickelDev
 * @since 03.03.2026
 **/
public final class OrbitDataSoA {

    int count;

    public int[] anchorIds;
    public int[] anchorToIndex;

    double[] a;
    double[] e;
    double[] t0Seconds;
    double[] omega;
    double[] meanMotion;
    double[] sqrtOneMinusE2;
    double[] cosOmega;
    double[] sinOmega;

    public OrbitDataSoA(int capacity) {
        anchorIds = new int[capacity];
        anchorToIndex = new int[capacity];
        Arrays.fill(anchorToIndex, -1);

        a = new double[capacity];
        e = new double[capacity];
        t0Seconds = new double[capacity];
        omega = new double[capacity];
        meanMotion = new double[capacity];
        sqrtOneMinusE2 = new double[capacity];
        cosOmega = new double[capacity];
        sinOmega = new double[capacity];

        count = 0;
    }

    public int getCount() {
        return count;
    }

    public void clear() {
        for (int i = 0; i < count; i++) {
            int anchor = anchorIds[i];
            anchorToIndex[anchor] = -1;
        }
        count = 0;
    }

    public void addEntity(int anchor, OrbitSolveInput input) {
        int idx = count++;

        if (anchorToIndex[anchor] != -1) {
            throw new IllegalStateException("Entity already in SoA: " + anchor);
        }

        anchorIds[idx] = anchor;
        anchorToIndex[anchor] = idx;

        populateSingle(idx, anchor, input);
    }

    public void removeEntity(int anchor) {
        if(count <= 0) throw new RuntimeException("Can't remove from empty " + this.getClass().getSimpleName());

        int idx = anchorToIndex[anchor];
        if (idx == -1) {
            Logger.get(LogTag.ORBT_SLVR).error("Tried removing invalid anchor -1 from " + this.getClass().getSimpleName());
            return;
        }

        int last = count - 1;

        if(idx != last) {
            int movedEntity = anchorIds[last];

            // move last entity into removed slot
            anchorIds[idx] = movedEntity;
            copyData(last, idx);
            anchorToIndex[movedEntity] = idx;
        }

        anchorToIndex[anchor] = -1;
        count--;
    }

    private void populateSingle(int idx, int anchor, OrbitSolveInput input) {
        int entity = input.orbitGraph().getAnchorToEntity()[anchor];

        OrbitDataComponent orbit = input.orbitData();

        double AU = Units.Length.AU.getBaseFactor();

        double aVal = orbit.getSemiMajorAxis(entity) * AU;
        double eVal = orbit.getEccentricity(entity);
        double omegaVal = orbit.getOmega(entity);

        int parent = orbit.getCentralBody(entity);
        double mu = Units.G_KM_TON * input.mass().getMass(parent);

        a[idx] = aVal;
        e[idx] = eVal;
        t0Seconds[idx] = orbit.getT0(entity) * 1e-6;
        omega[idx] = omegaVal;

        meanMotion[idx] = Math.sqrt(mu / (aVal*aVal*aVal));
        sqrtOneMinusE2[idx] = Math.sqrt(1 - (eVal*eVal));
        cosOmega[idx] = Math.cos(omegaVal);
        sinOmega[idx] = Math.sin(omegaVal);
    }

    private void copyData(int fromIdx, int toIdx) {
        a[toIdx] = a[fromIdx];
        e[toIdx] = e[fromIdx];
        t0Seconds[toIdx] = t0Seconds[fromIdx];
        omega[toIdx] = omega[fromIdx];
        meanMotion[toIdx] = meanMotion[fromIdx];
        sqrtOneMinusE2[toIdx] = sqrtOneMinusE2[fromIdx];
        cosOmega[toIdx] = cosOmega[fromIdx];
        sinOmega[toIdx] = sinOmega[fromIdx];
    }

//    public static int build(OrbitSolveInput input, OrbitDataSoA out) {
//        int count = input.usedCapacity();
//
//        OrbitDataComponent orbitDataRef = input.orbitData();
//
//        double AU = Units.Length.AU.getBaseFactor();
//
//        for (int i = 0; i < count; i++) {
//            int ent = input.entities()[i];
//            int p = input.orbitData().getCentralBody(ent);
//            out.entityIds[i] = ent;
//            out.a[i] = orbitDataRef.getSemiMajorAxis(ent) * AU;
//            out.e[i] = orbitDataRef.eccentricity[ent];
//            out.t0Seconds[i] = orbitDataRef.t0[ent] * 1e-6;
//            out.omega[i] = orbitDataRef.omega[ent];
//
//            double mu = Units.G_KM_TON * input.mass().getMass(p);
//            out.meanMotion[i] = Math.sqrt(mu / (out.a[i]*out.a[i]*out.a[i]));
//
//            out.sqrtOneMinusE2[i] = Math.sqrt(1 - (out.e[i]*out.e[i]));
//
//            out.cosOmega[i] = Math.cos(out.omega[i]);
//            out.sinOmega[i] = Math.sin(out.omega[i]);
//        }
//
//        out.count = count;
//
//        return count;
//    }
}
