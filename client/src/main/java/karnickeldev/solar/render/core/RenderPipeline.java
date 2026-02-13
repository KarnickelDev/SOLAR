package karnickeldev.solar.render.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 13.12.2025
 **/
public class RenderPipeline {

    private final List<RenderPass> pipeline = new ArrayList<>();

    public void add(RenderPass pass) {
        pipeline.add(pass);
    }

    public void render(RendererContext ctx, float delta) {
        for (RenderPass p : pipeline) {
            p.render(ctx, delta);
        }
    }

}
