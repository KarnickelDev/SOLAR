package karnickeldev.solar.ui.components.game;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.network.packets.SimTimeUpdateRequestPacket;
import karnickeldev.solar.simulation.execution.SimSpeedController;
import karnickeldev.solar.simulation.execution.SimulationManager;
import karnickeldev.solar.ui.components.UIComponent;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.screens.GameplayLoadScreen;

/**
 * @author : KarnickelDev
 * @since : 19.07.2025
 **/
public class TimeControl implements UIComponent {

    private final Table table;

    private int selectedSpeed = 1;

    private long lastUpdate = 0;

    public void setTargetSpeedIndex(byte index) {
        selectedSpeed = index;
        update(0);
    }

    private ButtonGroup<TextButton> speedButtons;

    public TimeControl() {
        table = new Table();
    }

    @Override
    public Group getGroup() {
        return table;
    }


    @Override
    public void update(float delta) {
        if(speedButtons.getCheckedIndex() != selectedSpeed) {
            resize(0,0);
        }
    }

    @Override
    public void resize(int width, int height) {
        table.clear();
        table.setSkin(UI.skin());

        table.setSize(300, 25);
        table.setPosition(0, UI.VIRTUAL_HEIGHT - 25);
        table.top().left().pad(0);

        speedButtons = new ButtonGroup<>();
        speedButtons.setMinCheckCount(1);
        speedButtons.setMaxCheckCount(1);
        speedButtons.setUncheckLast(true);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(UI.skin().get("toggle", TextButton.TextButtonStyle.class));
        style.font = UI.getFontManager().getFont(11, false);

        for(byte i = 0; i < SimulationManager.SPEEDS.length; i++) {
            byte speedIndex = i;
            TextButton button = new TextButton("" + i, style);
            button.pad(2);
            button.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    GameContext.get().getClientNetwork().send(new SimTimeUpdateRequestPacket(speedIndex, false));
                }
            });

            speedButtons.add(button);
            table.add(button).fill().expand();
        }

        speedButtons.uncheckAll();
        speedButtons.setChecked("" + selectedSpeed);

    }
}
