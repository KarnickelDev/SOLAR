package karnickeldev.solar.ui.layers.hud.chat;

import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.render.core.UIRenderer;
import karnickeldev.solar.ui.components.interaction.Scrollable;
import karnickeldev.solar.ui.components.widgets.Panel;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UIHelper;
import karnickeldev.solar.ui.components.UILayoutEngine;
import karnickeldev.solar.ui.core.UIManager;
import karnickeldev.solar.ui.fontutil.TextBlock;
import karnickeldev.solar.ui.fontutil.kernel.MSDFFont;
import karnickeldev.solar.util.MathUtil;

/**
 * @author KarnickelDev
 * @since 06.04.2026
 **/
public class MessageRenderer extends UIElement implements Scrollable {

    public static float DEFAULT_CHAT_FONT_SIZE = 14f;

    private static final float AUTO_SCROLL_THRESHOLD = 50f;
    private static final float AUTO_SCROLL_SPEED = 300f;
    private static final float MAX_SCROLL_SPEED = 10_000f;

    private MessageProvider messageProvider;

    private float scrollOffset = 0f;
    private float animationOffset = 0f;
    private float scrollVelocity = 0f;

    private float cAutoscrollThreshold = AUTO_SCROLL_THRESHOLD;

    private boolean autoScroll = true;
    private int unreadMessages = 0;

    private float paddingMessages = 0;
    private float padMessages = 0;

    private float totalHeight = 0f;

    public MessageRenderer(MessageProvider provider) {
        setTouchable(true);
        setMessageProvider(provider);
    }

    @Override
    public boolean onScrolled(float dx, float dy) {
        scrollVelocity += dy * 300f;
        scrollVelocity = MathUtil.clamp(scrollVelocity, -MAX_SCROLL_SPEED, MAX_SCROLL_SPEED);

        autoScroll = false;
        animationOffset = 0f;

        return true;
    }

    public void setMessageProvider(MessageProvider provider) {
        this.messageProvider = provider;
    }

    public void addMessage(TextBlock message) {
        float availableWidth = getContentWidth();
        float uiScale = UIManager.get().getLayoutContext().uiScaleY();

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
        setPadding(left, right, top, bottom);
        setMessagePadding(messages);
    }

    public void setMessagePadding(float padMessages) {
        paddingMessages = padMessages;
    }

    @Override
    public void onLayout(UILayoutEngine.UILayoutContext ctx) {
        padMessages = paddingMessages * ctx.uiScaleY();
        cAutoscrollThreshold = AUTO_SCROLL_THRESHOLD * ctx.uiScaleY();

        float availableWidth = getContentWidth();
        float uiScale = ctx.uiScaleY();

        for (int i = 0; i < messageProvider.size(); i++) {
            rebuildMessage(messageProvider.getMessage(i), SimTestScreen.font, availableWidth, uiScale);
        }
    }

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {

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
        draw(ctx);
    }

    public void draw(RendererContext ctx) {
        UIHelper.drawBackground(ctx.uiRenderer(), this, 0x0E0B0AC0, Panel.WHITE);
        UIHelper.drawBorder(ctx.uiRenderer(), this, 0xD94A3A2A, getBorderThickness(), Panel.WHITE);

        if (ctx.uiRenderer().pushScissors(getContentX(), getContentY(), getContentWidth(), getContentHeight())) {
            drawMessages(ctx.uiRenderer());
            ctx.uiRenderer().popScissors();
        }
    }

    private void drawMessages(UIRenderer uiRenderer) {
        MSDFFont font = SimTestScreen.font;

        float drawX = getContentX();
        float drawY = getContentY() + scrollOffset + animationOffset;
        float visibleTop = getContentTop();

        for (int i = messageProvider.size() - 1; i >= 0; i--) {
            TextBlock msg = messageProvider.getMessage(i);

            float messageHeight = msg.getLayout().getBoundsHeight();

            if (drawY + messageHeight < getContentY()) {
                drawY += messageHeight + padMessages;
                continue;
            }

            if (drawY > visibleTop) {
                break;
            }

            uiRenderer.drawText(font, msg.layout(font), drawX, drawY);

            drawY += messageHeight + padMessages;
        }
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
        float firstMsgHeight = 0f;
        if(messageProvider.size() > 0) {
            /*TODO: why does it not work without this?*/
            firstMsgHeight = messageProvider.getMessage(0).getLayout().getBoundsHeight();
        }

        float max = Math.max(0f, totalHeight - getHeight() + firstMsgHeight);
        scrollOffset = MathUtil.clamp(scrollOffset, -max, 0f);
    }

    private boolean isAtBottom() {
        return Math.abs(scrollOffset) < cAutoscrollThreshold;
    }
}
