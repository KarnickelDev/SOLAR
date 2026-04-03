package karnickeldev.solar.ecs;

import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;

/**
 * @author KarnickelDev
 * @since 16.08.2025
 **/
public enum Tag {

    GHOST_OBJECT("CELESTIAL_BODY_TYPE", true),
    STAR("CELESTIAL_BODY_TYPE", true),
    PLANET("CELESTIAL_BODY_TYPE", true),
    PLANETOID("", STAR, PLANET),
    ;

    public final String category;
    public final boolean exclusive;
    public final int bit;

    private int impliedMask = 0;

    Tag(String category, boolean exclusive) {
        if(ordinal() >= 32) throw new IllegalStateException("Exceeded Tag limit of 32!");

        this.category = category;
        this.exclusive = exclusive;
        this.bit = 1 << ordinal();
        this.impliedMask = bit;
    }

    Tag(String category, Tag... tags) {
        this(category, false);
        for(Tag t : tags) implies(t);
    }

    public Tag implies(Tag other) {
        if(other == null) {
            Logger.get(LogTag.ENTITY).warn("Tag is null");
        } else {
            this.impliedMask |= other.bit | other.impliedMask;
        }
        return this;
    }

    public int impliedMask() {
        return impliedMask;
    }

    public String category() {
        return category;
    }


    /** Tag Group, can be used to check for multiple Tags at once */
    public static class Group {

        public final String name;
        public final int mask;

        public Group(String name, Tag... tags) {
            int m = 0;
            for(Tag t : tags) m |= t.impliedMask;
            this.mask = m;
            this.name = name;
        }

        public boolean matches(int entityMask) {
            return (entityMask & mask) == mask;
        }

    }

}
