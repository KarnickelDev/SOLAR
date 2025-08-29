package karnickeldev.solar.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * @author : KarnickelDev
 * @since : 08.07.2025
 **/
public class GasGiantTest {

    public static void genPlanet(ShapeRenderer shapeRenderer, Viewport viewport, float time) {
        Color[] colors = new Color[]{
            new Color(0x511510FF),
            new Color(0x932A20FF),
        };

        int dim = 512;

        int bandHeight = 64;
        int border = 14;

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f,1f,1f,1f);
        for(int y = 0; y < dim; y++) {
            int dy = y - dim/2;
            int cY = 0;
            for(int x = 0; x < dim; x++) {
                int dx = x - dim/2;
                double dr = Math.sqrt(dx * dx + dy * dy);
                if(dr > dim/2f) continue;

                int scale = 6;
                int sX = (x/scale)*scale;
                int sY = (y/scale)*scale;

                float swirlStrength = 8f + 8 * (float) SimplexNoise.noise((time + sY/10f + sX/10f)/10f, sX / 200f);

                int X = (int) (sX + time);

                double noiseSwirl =
                    0.6f * SimplexNoise.noise(X * 0.002, sY * 0.002)
                        + 0.35f * SimplexNoise.noise(X * 0.008, sY * 0.008)
                        + 0.05f * SimplexNoise.noise(X * 0.02, sY * 0.02);

                float angle = (float) noiseSwirl * (float) (2 * Math.PI);
                float dY = (float) Math.sin(angle) * swirlStrength;

                float nY = sY + dY;

                int warpedY = (int) nY;

                // color
                int band = ((warpedY / bandHeight)+6) % 2;

                Color col = colors[band].cpy();

                cY = warpedY - (warpedY / bandHeight)*bandHeight;

                if(cY > bandHeight-border) {
                    col.lerp(colors[(band+2)%2], 1 - (Math.abs(bandHeight-cY) / (float)border));
                }
                if(cY < border){
                    col.lerp(colors[(band+1)%2], 1 - (Math.abs(cY) / (float)border));
                }

                float noise1 = (float) SimplexNoise.noise(sX/8f,sY/8f,0);
                float noise2 = (float) SimplexNoise.noise(sX/64f,sY/64f,200);
                float noise3 = (float) SimplexNoise.noise(sX/256f,sY/256f,400);

                float noise = 0.6f*noise3 + 0.25f*noise2 + 0.15f*noise1;

                float min = 0.75f;
                float v = min + (1f-min) * 0.5f*(noise+1);

                col.mul(v);

                // opacity
                float cutoff = 0.92f;
                double opacity = 1;
                double cr = dr / (0.5f*dim);
                if(cr >= cutoff) {
                    double o = 1 - cr;
                    opacity = o * (1/(1-cutoff));
                }
                col.a = (float)opacity;
                shapeRenderer.setColor(col);
                shapeRenderer.rect(800 + x, 300 + y, 1, 1);
            }
        }

        shapeRenderer.end();
        //test = new Texture(pix);
    }

}
