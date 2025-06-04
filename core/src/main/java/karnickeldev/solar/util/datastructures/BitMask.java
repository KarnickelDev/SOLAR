package karnickeldev.solar.util.datastructures;

public class BitMask {

    private int mask;

    public BitMask() {
        this(0);
    }

    public BitMask(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }

    public void set(int index) {
        mask = mask | (1 << index);
    }

    public void clear(int index) {
        mask = mask & ~(1 << index);
    }

    public boolean test(int index) {
        return 0 != ((mask >> index) & 1);
    }

}
