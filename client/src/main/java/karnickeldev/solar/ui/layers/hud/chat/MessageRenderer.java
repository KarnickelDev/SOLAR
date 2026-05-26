package karnickeldev.solar.ui.layers.hud.chat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.core.UILayoutEngine;
import karnickeldev.solar.ui.fontutil.TextBlock;
import karnickeldev.solar.ui.fontutil.kernel.MSDFFont;
import karnickeldev.solar.util.MathUtil;

/**
 * @author KarnickelDev
 * @since 06.04.2026
 **/
public class MessageRenderer extends UIElement {

    public static float DEFAULT_CHAT_FONT_SIZE = 11f;

    private static final float AUTO_SCROLL_THRESHOLD = 50f;
    private static final float AUTO_SCROLL_SPEED = 300f;
    private static final float MAX_SCROLL_SPEED = 10_000f;

    private MessageProvider messageProvider;

    private float scrollOffset = 0f;
    private float animationOffset = 0f;
    private float scrollVelocity = 0f;

    private boolean autoScroll = true;
    private int unreadMessages = 0;

    private float paddingLeft = 0;
    private float paddingRight = 0;
    private float paddingTop = 0;
    private float paddingBottom = 0;
    private float paddingMessages = 0;

    private float padLeft = 0;
    private float padRight = 0;
    private float padTop = 0;
    private float padBottom = 0;
    private float padMessages = 0;

    private float v_borderThickness = 0;
    private float borderThickness = 0;

    private final Vector2 tmpVec = new Vector2();
    private final Rectangle bounds = new Rectangle();

    private float totalHeight = 0f;

    public MessageRenderer(MessageProvider provider) {
        setTouchable(true);
        setMessageProvider(provider);
    }

    @Override
    public boolean handleInput(InputEvent e) {
        if (e.getType() == InputEvent.Type.scrolled) {
            scrollVelocity += e.getScrollAmountY() * 300f;
            scrollVelocity = MathUtil.clamp(scrollVelocity, -MAX_SCROLL_SPEED, MAX_SCROLL_SPEED);

            autoScroll = false;
            animationOffset = 0f;

            return true;
        }

        return false;
    }

    public void setMessageProvider(MessageProvider provider) {
        this.messageProvider = provider;
    }

    public void addMessage(TextBlock message) {
        float availableWidth = getWidth() - padLeft - padRight;
        float uiScale = UILayoutEngine.getUIScaleY();

        rebuildMessage(message, SimTestScreen.font, availableWidth, uiScale);

        TextBlock removed = messageProvider.addMessage(message);
        if (removed != null) {
            totalHeight -= removed.getLayout().getBoundsHeight() + padMessages;
        }

        boolean wasAtBottom = isAtBottom();

        if (!autoScroll && !wasAtBottom) {
            unreadMessages++;
        } else {
            autoScroll = true;
        }

        float addedHeight = message.getLayout().getBoundsHeight() + padMessages;
        totalHeight += addedHeight;

        if (wasAtBottom) {
            animationOffset -= addedHeight;
        } else {
            scrollOffset -= addedHeight;
        }
    }

    public void setPad(float left, float right, float top, float bottom, float messages) {
        paddingLeft = left;
        paddingRight = right;
        paddingTop = top;
        paddingBottom = bottom;
        paddingMessages = messages;

        padLeft = (borderThickness + paddingLeft) * UILayoutEngine.getUIScaleY();
        padRight = (borderThickness + paddingRight) * UILayoutEngine.getUIScaleY();
        padTop = (borderThickness + paddingTop) * UILayoutEngine.getUIScaleY();
        padBottom = (borderThickness + paddingBottom) * UILayoutEngine.getUIScaleY();
        padMessages = paddingMessages * UILayoutEngine.getUIScaleY();
    }

    public void setBorderThickness(float borderThickness) {
        this.v_borderThickness = borderThickness;
        this.borderThickness = borderThickness * UILayoutEngine.getUIScaleY();
        setPad(paddingLeft, paddingRight, paddingTop, paddingBottom, paddingMessages);
    }

    @Override
    public void layout(float width, float height, float scale) {
        setBorderThickness(v_borderThickness);
        super.layout(width, height, scale);

        float availableWidth = getWidth() - padLeft - padRight;
        float uiScale = UILayoutEngine.getUIScaleY();

        for (int i = 0; i < messageProvider.size(); i++) {
            rebuildMessage(messageProvider.getMessage(i), SimTestScreen.font, availableWidth, uiScale);
        }
    }

    @Override
    public void act(float delta) {
        recomputeTotalHeight();

        scrollVelocity = MathUtil.clamp(scrollVelocity, -MAX_SCROLL_SPEED, MAX_SCROLL_SPEED);

        if (autoScroll) {
            scrollVelocity = AUTO_SCROLL_SPEED;
        }

        scrollOffset += scrollVelocity * delta;
        clampScroll();

        if (isAtBottom()) {
            unreadMessages = 0;

            if (scrollVelocity >= 0f) {
                autoScroll = true;
            }
        }

        animationOffset *= (float) Math.pow(0.90f, delta * 200f);
        if (Math.abs(animationOffset) < 1f) {
            animationOffset = 0f;
        }

        scrollVelocity *= (float) Math.pow(0.90f, delta * 70f);
        if (Math.abs(scrollVelocity) < 1f) {
            scrollVelocity = 0f;
        }
    }

    @Override
    public void render(RendererContext ctx) {
        draw(ctx.batch(), 1f);
    }

    public void draw(Batch batch, float parentAlpha) {
        Drawable background = UI.skin().getDrawable("default-pane-noborder");

        batch.setColor(new Color(0x0E0B0AC0));
        background.draw(batch, getX(), getY(), getWidth(), getHeight());

        batch.setColor(new Color(0xD94A3A2A));
        background.draw(batch, getX(), getY(), getWidth(), borderThickness);
        background.draw(batch, getX(), getY() + borderThickness, borderThickness, getHeight() - 2 * borderThickness);
        background.draw(batch, getX(), getTop() - borderThickness, getWidth(), borderThickness);
        background.draw(batch, getRight() - borderThickness, getY() + borderThickness, borderThickness, getHeight() - 2 * borderThickness);

        batch.setColor(Color.WHITE);
        batch.flush();

        tmpVec.set(getX() + padLeft, getY() + padBottom);

        bounds.set(
            tmpVec.x,
            tmpVec.y,
            getWidth() - padLeft - padRight,
            getHeight() - padTop - padBottom
        );

        if (ScissorStack.pushScissors(bounds)) {
            drawMessages();
            batch.flush();
            ScissorStack.popScissors();
        }
    }

    private void drawMessages() {
        MSDFFont font = SimTestScreen.font;

        float drawX = getX() + padLeft;
        float drawY = getY() + padBottom + scrollOffset + animationOffset;
        float visibleTop = getY() + getHeight() - padTop;

        SimTestScreen.msdfBatch.begin();

        for (int i = messageProvider.size() - 1; i >= 0; i--) {
            TextBlock msg = messageProvider.getMessage(i);

            float messageHeight = msg.getLayout().getBoundsHeight();

            if (drawY + messageHeight < getY() + padBottom) {
                drawY += messageHeight + padMessages;
                continue;
            }

            if (drawY > visibleTop) {
                break;
            }

            SimTestScreen.msdfBatch.draw(font, msg.layout(font), drawX, drawY);

            drawY += messageHeight + padMessages;
        }

        SimTestScreen.msdfBatch.end();
    }

    private void rebuildMessage(TextBlock message, MSDFFont font, float availableWidth, float uiScale) {
        message.setMaxWidth(availableWidth);
        message.setUiScale(uiScale);
        message.layout(font);
    }

    private void recomputeTotalHeight() {
        totalHeight = 0f;

        for (int i = 0; i < messageProvider.size(); i++) {
            TextBlock msg = messageProvider.getMessage(i);
            totalHeight += msg.getLayout().getBoundsHeight();

            if (i != messageProvider.size() - 1) {
                totalHeight += padMessages;
            }
        }
    }

    private void clampScroll() {
        float max = Math.max(0f, totalHeight - getHeight() + padTop + padBottom);
        scrollOffset = MathUtil.clamp(scrollOffset, -max, 0f);
    }

    private boolean isAtBottom() {
        return Math.abs(scrollOffset) < AUTO_SCROLL_THRESHOLD * UILayoutEngine.getUIScaleY();
    }
}
