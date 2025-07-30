package karnickeldev.solar.ui.components.game;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ui.components.UIComponent;
import karnickeldev.solar.ui.core.UI;

/**
 * @author : KarnickelDev
 * @since : 29.07.2025
 **/
public class DateDisplay implements UIComponent {

    private static final float UPDATE_RATE_SECONDS = 1 / 60f;
    private static final short START_YEAR = 2250;

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int DAYS_PER_MONTH = 30;
    private static final int DAYS_PER_YEAR = 365;

    private float accumulator = 0;

    private Table group;

    private Label dateLabel;
    private Label timeLabel;
    private Label secondsLabel;

    private int year;
    private byte month;
    private byte day;

    private byte hour;
    private byte minute;
    private byte second;

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
        timeLabel.setText(getTimeText());
        secondsLabel.setText(getSecondsText());
    }

    @Override
    public void resize(int width, int height) {
        group.clear();
        group.setSkin(UI.skin());
        group.setBackground(UI.skin().get("up", NinePatchDrawable.class));
        group.setSize(120,60);
        group.setPosition(UI.VIRTUAL_WIDTH - group.getWidth(), UI.VIRTUAL_HEIGHT - group.getHeight());
        group.center().bottom().pad(10);

        Label.LabelStyle style = new Label.LabelStyle(UI.getFontManager().getFont(12, false), UI.WHITE);
        Label.LabelStyle styleSeconds = new Label.LabelStyle(UI.getFontManager().getFont(8, false), UI.WHITE);

        dateLabel = new Label(getDateText(), style);
        dateLabel.setAlignment(Align.left);
        dateLabel.setEllipsis(true);

        timeLabel = new Label(getTimeText(), style);
        timeLabel.setAlignment(Align.bottomLeft);
        timeLabel.setEllipsis(true);

        secondsLabel = new Label(getSecondsText(), styleSeconds);
        secondsLabel.setAlignment(Align.bottomLeft);
        secondsLabel.setEllipsis(true);

        group.add(dateLabel).fill().expand();
        group.row();

        Table timeGroup = new Table();
        timeGroup.pad(0);
        timeGroup.bottom().left();
        timeGroup.add(timeLabel).width(55).fill().left();
        timeGroup.add();
        timeGroup.add(secondsLabel).height(20).right();

        group.add(timeGroup).expand().fill();
    }

    private String getDateText() {
        return String.format("%02d.%02d.%04d", day, month, year);
    }

    private String getTimeText() {
        return String.format("%02d:%02d", hour, minute);
    }

    private String getSecondsText() {
        return String.format("%02d", second);
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
        second = (byte) (seconds % SECONDS_PER_MINUTE);
    }

}
