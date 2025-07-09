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
import karnickeldev.solar.network.util.PingTracker;
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

        table.setFillParent(false);
        table.top().left();
    }

    public void update(float delta) {
        fps.setVisible(true);
        tps.setVisible(false);
        ping.setVisible(false);

        fps.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        ping.setColor(PingTracker.getPing() > 2 * PingTracker.getStdDev() ? Color.ORANGE : Color.GREEN);

        if(GameStateManager.get().getState().getID() == GameStateID.GAMEPLAY) {
            tps.setVisible(true);
            tps.setText("TPS: " + SolarMain.tps);

            if(GameContext.get().isMultiplayer()) {
                ping.setText("Ping: " + PingTracker.getPing() + "ms");
                ping.setVisible(true);
            }
        }


        table.layout();
    }

    public void resize(int width, int height) {
        Label.LabelStyle labelStyle = new Label.LabelStyle(UI.getFontManager().getFont(14, false), Color.GREEN);
        fps = new Label("FPS: 9999", labelStyle);
        tps = new Label("TPS: 999", labelStyle);
        ping = new Label("Ping: 999ms", labelStyle);

        fps.setAlignment(Align.left);
        tps.setAlignment(Align.left);
        ping.setAlignment(Align.left);

        table.clear();
        table.add(fps).pad(5).padRight(1).row();
        table.add(tps).pad(5).padRight(1).row();
        table.add(ping).pad(5).padRight(1).row();

        table.setSize(table.getPrefWidth(),table.getPrefHeight());
        table.setPosition(UI.VIRTUAL_WIDTH - table.getWidth(), UI.VIRTUAL_HEIGHT - table.getHeight());

        table.layout();
    }

    public Group getGroup() {
        return table;
    }

}
