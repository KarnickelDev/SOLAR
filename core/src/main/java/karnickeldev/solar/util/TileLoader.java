package karnickeldev.solar.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 15.10.2024
 */
public class TileLoader {


    public static TextureRegion[] getTiles(Texture atlas, int tile_count, int tile_size) {
        TextureRegion[] background_tiles = new TextureRegion[tile_count];
        int count = 0;
        for (int y = 0; y < tile_count / 2; y++) {
            for (int x = 0; x < tile_count / 2; x++) {
                background_tiles[count++] = new TextureRegion(atlas,
                    x * tile_size, y * tile_size,
                    tile_size, tile_size);
            }
        }
        return background_tiles;
    }

    public static void renderBackground(SpriteBatch batch, TextureRegion[] background_tiles, int width, int height) {
        if (batch == null || background_tiles == null) throw new RuntimeException("Batch or Tiles is null");
        int tile_size = background_tiles[0].getRegionWidth();
        batch.begin();
        int i = 0;
        for (int y = 0; y <= 1 + height / tile_size; y++) {
            for (int x = 0; x <= 1 + width / tile_size; x++) {
                batch.draw(background_tiles[(3 * x + 8 * y + (x * y) * x + 5 + y * y + 19 + i) % background_tiles.length],
                    x * tile_size, y * tile_size,
                    tile_size, tile_size);
                i++;
                if (i >= background_tiles.length) i = 0;
            }
        }
        batch.end();
    }

}
