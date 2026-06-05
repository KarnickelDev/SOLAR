package karnickeldev.solar.render.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import karnickeldev.solar.ui.fontutil.kernel.MSDFBatch;
import karnickeldev.solar.ui.fontutil.kernel.MSDFFont;
import karnickeldev.solar.ui.fontutil.kernel.TextLayout;

/**
 * @author KarnickelDev
 * @since 06.06.2026
 **/
public final class UIRenderer {

    private final SpriteBatch quadBatch;
    private final MSDFBatch msdfBatch;

    private final Matrix4 matrix = new Matrix4();

    private boolean quadMode = true;
    private boolean drawing = false;

    public UIRenderer(SpriteBatch quadBatch, MSDFBatch msdfBatch) {
        this.quadBatch = quadBatch;
        this.msdfBatch = msdfBatch;
    }

    public void updateViewport(float x, float y, float width, float height) {
        matrix.setToOrtho2D(x, y, width, height);
        quadBatch.setProjectionMatrix(matrix);
        msdfBatch.setProjectionMatrix(matrix);
    }

    public void setQuadColor(Color color) {
        quadBatch.setColor(color);
    }

    public boolean pushScissors(float x, float y, float width, float height) {
        quadBatch.flush();
        msdfBatch.flush();
        return ScissorStack.pushScissors(new Rectangle(x, y, width, height));
    }

    public void popScissors() {
        quadBatch.flush();
        msdfBatch.flush();
        ScissorStack.popScissors();
    }

    public void begin() {
        if(drawing) throw new IllegalStateException("Must call end before begin");
        if(quadBatch.isDrawing() || msdfBatch.isDrawing()) throw new IllegalStateException("Invalid batch state before begin");

        drawing = true;

        if(quadMode) {
            quadBatch.begin();
        } else {
            msdfBatch.begin();
        }
    }

    public void end() {
        if(!drawing) throw new IllegalStateException("Must call begin before end");
        drawing = false;

        if(quadMode) {
            quadBatch.end();
        } else {
            msdfBatch.end();
        }

        if(quadBatch.isDrawing() || msdfBatch.isDrawing()) throw new IllegalStateException("Invalid batch state after end");
    }

    public void drawQuad(Texture texture, float x, float y, float width, float height) {
        if(!quadMode) {
            msdfBatch.end();
            quadBatch.begin();

            quadMode = true;
        }

        quadBatch.draw(texture, x, y, width, height);
    }

    public void drawText(MSDFFont font, TextLayout text, float x, float y) {
        if(quadMode) {
            quadBatch.end();
            msdfBatch.begin();

            quadMode = false;
        }

        msdfBatch.draw(font, text, x, y);
    }

}
