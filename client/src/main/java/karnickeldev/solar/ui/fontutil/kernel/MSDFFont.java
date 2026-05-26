package karnickeldev.solar.ui.fontutil.kernel;

import com.badlogic.gdx.graphics.Texture;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KarnickelDev
 * @since 14.04.2026
 **/
public final class MSDFFont {

    private static final int MISSING_GLYPH_INDEX = 0;

    private final Texture atlas;
    String name = "EMPTY";

    // dense glyph storage
    private Glyph[] glyphs;
    private int glyphCount = 1; /** Start at 1 to leave space for MISSING_GLYPH */

    private final Map<Integer, Integer> codepointToGlyphIndex =  new HashMap<>(128);

    // fast ascii lookup path
    private final short[] asciiLookup = new short[256];


    // font metrics
    private float lineHeight;
    private float ascent;
    private float descent;
    private float spaceAdvance;
    private float pxRange;

    public MSDFFont(Texture atlas, int initialGlyphCapacity) {
        this.atlas = atlas;
        this.glyphs = new Glyph[Math.max(32, initialGlyphCapacity)];

        Arrays.fill(asciiLookup, (short)-1);
    }

    public String getName() {
        return name;
    }

    public Glyph[] getGlyphs() {
        return glyphs;
    }

    public void setMetrics(float lineHeight, float ascent, float descent, float spaceAdvance, float pxRange) {
        this.lineHeight = lineHeight;
        this.ascent = ascent;
        this.descent = descent;
        this.spaceAdvance = spaceAdvance;
        this.pxRange = pxRange;
    }

    public void setDefaultGlyph(Glyph glyph) {
        //glyph.index = 0;
        glyphs[0] = glyph;
        if (glyph.codepoint >= 0 && glyph.codepoint < asciiLookup.length) {
            asciiLookup[glyph.codepoint] = 0;
        } else {
            codepointToGlyphIndex.put(glyph.codepoint, 0);
        }
    }

    public void addGlyph(Glyph glyph) {
        ensureGlyphCapacity(glyphCount + 1);

        //glyph.index = glyphCount;
        glyphs[glyphCount] = glyph;

        if (glyph.codepoint >= 0 && glyph.codepoint < asciiLookup.length) {
            asciiLookup[glyph.codepoint] = (short)glyphCount;
        } else {
            codepointToGlyphIndex.put(glyph.codepoint, glyphCount);
        }

        glyphCount++;
    }

    public Glyph getGlyph(int codepoint) {
        return glyphs[getGlyphIndex(codepoint)];
    }

    public Glyph getGlyphUnsafe(int index) {
        return glyphs[index];
    }

    public int getGlyphIndex(int codepoint) {
        if ((codepoint & ~0xFF) == 0) {
            int idx = asciiLookup[codepoint];
            if (idx >= 0) {
                return idx;
            }
        }

        return codepointToGlyphIndex.getOrDefault(codepoint, MISSING_GLYPH_INDEX);
    }

    public Glyph getGlyphByIndex(int glyphIndex) {
        if(glyphIndex >= glyphCount) {
            return glyphs[MISSING_GLYPH_INDEX];
        }
        return glyphs[glyphIndex];
    }

    public float getKerning(int leftCodepoint, int rightCodepoint) {
        return 0f;
    }

    public Texture getAtlas() {
        return atlas;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public float getAscent() {
        return ascent;
    }

    public float getDescent() {
        return descent;
    }

    public float getSpaceAdvance() {
        return spaceAdvance;
    }

    public float getPxRange() {
        return pxRange;
    }

    public int getGlyphCount() {
        return glyphCount;
    }

    private void ensureGlyphCapacity(int required) {
        if (required <= glyphs.length) {
            return;
        }

        int newCapacity = glyphs.length;
        while (newCapacity < required) {
            newCapacity *= 2;
        }

        glyphs = Arrays.copyOf(glyphs, newCapacity);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int asciiCount = 0;
        for(int i = 0; i < asciiLookup.length; i++) {
            if (asciiLookup[i] >= 0) asciiCount++;
        }

        sb.append("MSDFFont {\n");
        sb.append("name=").append("\"").append(name).append("\"\n");
        sb.append("glyphCount=").append(glyphCount-1).append(", asciiCount=").append(asciiCount).append("\n");
        sb.append("asciiGlyphs=[\n");
        int j = 0;
        for(int i = 0; i < asciiLookup.length; i++) {
            if(asciiLookup[i] < 0) continue;
            j++;
            System.out.println(asciiLookup[i]);
            sb.append(getGlyphByIndex(asciiLookup[i]).codepoint);
            if(j < asciiCount) sb.append(", ");
        }
        sb.append("\n],\n");
        sb.append("glyphs=[\n");
        Object[] keys = codepointToGlyphIndex.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i].toString());
            if(i < keys.length - 1) sb.append(", ");
        }
        sb.append("\n]\n");
        sb.append('}');

        return sb.toString();
    }

}
