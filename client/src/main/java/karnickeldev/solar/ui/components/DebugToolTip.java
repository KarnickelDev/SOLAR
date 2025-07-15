package karnickeldev.solar.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.network.net.core.PingTracker;
import karnickeldev.solar.ui.core.UI;

/**
 * @author : KarnickelDev
 * @since : 03.07.2025
 **/
public class DebugToolTip implements UIComponent {

    private final Table table;

    private Label fps;
    private Label tps;
    private Label ping;

    public DebugToolTip() {
        table = new Table();
    }

    private long lastUpdate = 0;

    public void update(float delta) {
        long now = System.nanoTime();
        if(now - lastUpdate <= 300_000_000) return;
        lastUpdate = now;

        fps.setVisible(true);
        tps.setVisible(false);
        ping.setVisible(false);

        fps.setColor(Color.GREEN);
        fps.setText("FPS: " + Gdx.graphics.getFramesPerSecond());

        if(GameStateManager.get().getState().getID() == GameStateID.GAMEPLAY) {
            tps.setVisible(true);
            tps.setText("TPS: " + SolarMain.tps);
            tps.setColor(Color.GREEN);

            if(GameContext.get().isMultiplayer()) {
                ping.setText("Ping: " + PingTracker.getPing() + "ms");
                ping.setVisible(true);
                ping.setColor(PingTracker.getStdDev() > 10 ? Color.ORANGE : Color.GREEN);
            }
        }
    }

    public void resize(int width, int height) {
        Label.LabelStyle labelStyle = new Label.LabelStyle(UI.getFontManager().getFont(11, false), Color.WHITE);
        fps = new Label("FPS: 9999", labelStyle);
        tps = new Label("TPS: 999", labelStyle);
        ping = new Label("Ping: 999ms", labelStyle);

        fps.setAlignment(Align.left);
        tps.setAlignment(Align.left);
        ping.setAlignment(Align.left);

        table.clear();
        table.top().left().pad(2);
        table.add(fps).fill().row();
        table.add(tps).fill().row();
        table.add(ping).fill().row();

        table.setSize(table.getPrefWidth(),table.getPrefHeight());
        table.setPosition(UI.VIRTUAL_WIDTH - table.getWidth(), UI.VIRTUAL_HEIGHT - table.getHeight());

        table.layout();
    }

    public Group getGroup() {
        return table;
    }

}
