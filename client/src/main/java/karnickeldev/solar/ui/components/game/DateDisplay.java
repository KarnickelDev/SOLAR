package karnickeldev.solar.ui.components.game;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ui.components.UIComponent;
import karnickeldev.solar.ui.core.FontManager;
import karnickeldev.solar.ui.core.UI;

/**
 * @author : KarnickelDev
 * @since : 29.07.2025
 **/
public class DateDisplay implements UIComponent {

    private static final float UPDATE_RATE_SECONDS = 1 / 50f;
    private static final short START_YEAR = 2250;

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int DAYS_PER_MONTH = 30;
    private static final int DAYS_PER_YEAR = 365;

    private float accumulator = 0;

    private final Table group;

    private Label dateLabel;
    private Label hourLabel;
    private Label minuteLabel;

    private Label speedLabel;

    private TextButton pauseButton;

    private int year;
    private byte month;
    private byte day;

    private byte hour;
    private byte minute;

    public DateDisplay() {
        group = new Table();
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public void update(float delta) {
        accumulator += delta;
        if(accumulator < UPDATE_RATE_SECONDS) return;

        // reset update rate accumulator
        accumulator = 0;

        // update date
        updateDate();

        dateLabel.setText(getDateText());
        hourLabel.setText(getHourText());
        minuteLabel.setText(getMinuteText());

        // update pause button
        pauseButton.setText(GameContext.get().getClock().isPaused() ? "\uf04b" : "\uf04c");

        // update speed
        String[] speedLabels = {"slow", "normal", "fast", "fastest"};
        speedLabel.setText(speedLabels[Math.min(GameContext.get().getClock().getTargetSimSpeedIndex(), speedLabels.length-1)]);
    }

    @Override
    public void resize(int width, int height) {
        group.clear();
        group.setSkin(UI.skin());
        group.setBackground(UI.skin().get("up", NinePatchDrawable.class));
        group.setSize(200,70);
        group.setPosition(UI.VIRTUAL_WIDTH - group.getWidth(), UI.VIRTUAL_HEIGHT - group.getHeight());
        group.center().pad(10).padBottom(2).padTop(2);

        TextButton.TextButtonStyle toggleStyle = new TextButton.TextButtonStyle(UI.skin().get("default", TextButton.TextButtonStyle.class));
        toggleStyle.font = UI.getFontManager().getFont(FontManager.Fonts.FONT_AWESOME, 22, false);
        toggleStyle.checked = null;
        toggleStyle.up = null;
        toggleStyle.down = null;

        pauseButton = new TextButton("\uf04b", toggleStyle);
        pauseButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                GameContext.get().getClock().getSimSpeedController().togglePause();
            }
        });

        group.add(pauseButton).expand().fill().width(40).height(40);

        Label.LabelStyle style = new Label.LabelStyle(UI.getFontManager().getFont(10, false), UI.WHITE);

        dateLabel = new Label(getDateText(), style);
        dateLabel.setAlignment(Align.center);
        dateLabel.setEllipsis(true);

        hourLabel = new Label(getHourText(), style);
        hourLabel.setAlignment(Align.left);
        hourLabel.setEllipsis(true);

        minuteLabel = new Label(getMinuteText(), style);
        minuteLabel.setAlignment(Align.left);
        minuteLabel.setEllipsis(true);

        Table timeSubGroup = new Table();
        timeSubGroup.pad(0).center();
        float timeWidth = 1.2f * minuteLabel.getMinWidth();
        timeSubGroup.add(hourLabel).width(timeWidth);
        timeSubGroup.add(new Label(":", style)).padLeft(4).padRight(4);
        timeSubGroup.add(minuteLabel).width(timeWidth);

        speedLabel = new Label("normal", new Label.LabelStyle(UI.getFontManager().getFont(10), UI.WHITE));
        speedLabel.setAlignment(Align.center);

        VerticalGroup timeDateGroup = new VerticalGroup();
        timeDateGroup.pad(2).center();
        timeDateGroup.addActor(dateLabel);
        timeDateGroup.addActor(timeSubGroup);
        timeDateGroup.addActor(speedLabel);

        group.add(timeDateGroup).expand();

        TextButton plusSpeed = new TextButton("+", UI.skin());
        plusSpeed.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                GameContext.get().getClock().getSimSpeedController().changeSpeed(+1);
            }
        });

        TextButton minusSpeed = new TextButton("-", UI.skin());
        minusSpeed.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                GameContext.get().getClock().getSimSpeedController().changeSpeed(-1);
            }
        });

        Table speedMod = new Table();
        speedMod.add(plusSpeed).height(24).width(24).pad(4);
        speedMod.row();
        speedMod.add(minusSpeed).height(24).width(24).pad(4);

        group.add(speedMod).expand();

        group.layout();
    }

    private String getDateText() {
        return String.format("%02d.%02d.%04d", day, month, year);
    }

    private String getHourText() {
        return String.format("%02d", hour);
    }

    private String getMinuteText() {
        return String.format("%02d", minute);
    }

    private void updateDate() {
        long totalSeconds = GameContext.get().getClock().nowSimSeconds();

        long totalDays = totalSeconds / SECONDS_PER_DAY;
        long remainingSeconds = totalSeconds % SECONDS_PER_DAY;
        long dayOfYear = totalDays % DAYS_PER_YEAR;
        long seconds = remainingSeconds % SECONDS_PER_HOUR;

        year = START_YEAR + (int) (totalDays / DAYS_PER_YEAR + 1);
        month = (byte) (dayOfYear / DAYS_PER_MONTH + 1);
        day = (byte) (dayOfYear % DAYS_PER_MONTH + 1);

        hour = (byte) (remainingSeconds / SECONDS_PER_HOUR);
        minute = (byte) (seconds / SECONDS_PER_MINUTE);
    }
}
