package karnickeldev.solar.ui.fontutil.kernel;

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

            for (JsonValue g : root.get("glyphs")) {
                int codepoint = g.getInt("unicode");

                float advance = g.getFloat("advance");
                float planeLeft = 0, planeRight = 0, planeTop = 0, planeBottom = 0;
                float u0 = 0, u1 = 0, v0 = 0, v1 = 0;
                short atlasPage = 0;

                JsonValue pb = g.get("planeBounds");
                if(pb != null) {
                    planeLeft = pb.getFloat("left",0);
                    planeBottom = pb.getFloat("bottom",0);
                    planeRight = pb.getFloat("right",0);
                    planeTop = pb.getFloat("top",0);
                }
                JsonValue ab = g.get("atlasBounds");
                if(ab != null) {
                    u0 = ab.getFloat("left", 0) / width;
                    u1 = ab.getFloat("right",0) / width;

                    if (yOrigin.equalsIgnoreCase("bottom")) {
                        v0 = 1f - (ab.getFloat("bottom") / height);
                        v1 = 1f - (ab.getFloat("top") / height);
                    } else {
                        v0 = ab.getFloat("bottom") / height;
                        v1 = ab.getFloat("top") / height;
                    }
                }

                font.addGlyph(new Glyph(codepoint, advance, planeLeft, planeBottom, planeRight, planeTop, u0, v0, u1, v1, atlasPage));
            }

            // ADD FALLBACK GLYPH
            Glyph q = font.getGlyph('?');
            Glyph missing = new Glyph('?', q.advance,
                q.planeLeft, q.planeBottom, q.planeRight, q.planeTop,
                q.u0, q.v0, q.u1, q.v1, q.atlasPage
            );

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
