package karnickeldev.solar.ui.fontutil;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;

/**
 * @author KarnickelDev
 * @since 14.04.2026
 **/
public class JSONLoader {

    public static MSDFFont loadFont(FileHandle file, Texture atlas) {
        try {
            MSDFFont font = new MSDFFont(atlas, 256);

            Json json = new Json();
            JsonValue root = json.fromJson(null, file.reader());

            String name = root.getString("name", "NOT_FOUND");
            font.name = name;

            JsonValue m = root.get("metrics");

            float lineHeight = m.getFloat("lineHeight");
            float ascender = m.getFloat("ascender");
            float descender = m.getFloat("descender");

            JsonValue at = root.get("atlas");

            float width = at.getFloat("width");
            float height = at.getFloat("height");

            String yOrigin = at.getString("yOrigin", "bottom");

            float pxRange = at.getFloat("distanceRange", 6);

            float spaceAdvance = 0f;

            for (JsonValue g : root.get("glyphs")) {
                int codepoint = g.getInt("unicode");

                Glyph glyph = new Glyph(codepoint);

                glyph.advance = g.getFloat("advance");

                JsonValue pb = g.get("planeBounds");
                if(pb != null) {
                    glyph.planeLeft = pb.getFloat("left",0);
                    glyph.planeBottom = pb.getFloat("bottom",0);
                    glyph.planeRight = pb.getFloat("right",0);
                    glyph.planeTop = pb.getFloat("top",0);
                }
                JsonValue ab = g.get("atlasBounds");
                if(ab != null) {
                    glyph.u0 = ab.getFloat("left", 0) / width;
                    glyph.u1 = ab.getFloat("right",0) / width;

                    if (yOrigin.equalsIgnoreCase("bottom")) {
                        glyph.v0 = 1f - (ab.getFloat("bottom") / height);
                        glyph.v1 = 1f - (ab.getFloat("top") / height);
                    } else {
                        glyph.v0 = ab.getFloat("bottom") / height;
                        glyph.v1 = ab.getFloat("top") / height;
                    }
                }

                font.addGlyph(glyph);
            }

            // ADD FALLBACK GLYPH
            Glyph q = font.getGlyph('?');
            Glyph missing = new Glyph('?');
            missing.advance = q.advance;
            missing.atlasPage = q.atlasPage;
            missing.u0 = q.u0;
            missing.v0 = q.v0;
            missing.u1 = q.u1;
            missing.v1 = q.v1;
            missing.planeLeft = q.planeLeft;
            missing.planeBottom = q.planeBottom;
            missing.planeRight = q.planeRight;
            missing.planeTop = q.planeTop;

            font.setDefaultGlyph(missing);

            font.setMetrics(lineHeight, ascender, descender, font.getGlyph(' ').advance, pxRange);

            Logger.get(LogTag.UI).info("MSDF Font \"{}\" loaded successfully", name);
            Logger.get(LogTag.UI).debug(font.toString());

            return font;
        } catch (Exception e) {
            if(file != null) {
                Logger.get(LogTag.UI).error("Error while loading Font from JSON file " + file.name(), e);
            } else {
                Logger.get(LogTag.UI).error("Font file is null", e);
            }
            return null;
        }
    }

}
