package karnickeldev.solar.ui.layers.hud.chat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import karnickeldev.solar.ui.core.FontManager;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.util.MathUtil;

/**
 * @author KarnickelDev
 * @since 06.04.2026
 **/
public class MessageRenderer extends Widget {

    private static final float AUTO_SCROLL_THRESHOLD = 50f;
    private static final float AUTO_SCROLL_SPEED = 300f;
    private static final float MAX_SCROLL_SPEED = 10_000;

    private static final Color NOTIFY_COLOR = new Color(0xFFC857FF);

    private static final int fontSize = 18;

    private MessageProvider messageProvider;

    private float scrollOffset = 0;
    private float scrollVelocity = 0;
    private boolean autoScroll = true;
    private int unreadMessages = 0;

    private float padLeft = 0;
    private float padRight = 0;
    private float padTop = 0;
    private float padBottom = 0;
    private float padMessages = 0;

    private final Vector2 tmpVec = new Vector2();
    private final Rectangle scissors = new Rectangle();
    private final Rectangle bounds = new Rectangle();

    private float totalHeight = 0;

    public MessageRenderer(MessageProvider provider) {
        setTouchable(Touchable.enabled);

        addListener(new InputListener() {

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                // down is positive amount
                scrollVelocity += amountY * 300;
                autoScroll = false;
                return true;
            }
        });

        setMessageProvider(provider);

        setX(0);
        setY(0);
        setWidth(400);
        setHeight(400);
    }

    public void setMessageProvider(MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    public void addMessage(ChatMessage message) {
        message.layout(UI.getFontManager().getFont(FontManager.Fonts.MARTIAN, fontSize, false), getWidth() - padLeft - padRight);

        ChatMessage oldMsg = messageProvider.addMessage(message);
        if(oldMsg != null) {
            totalHeight -= oldMsg.getTotalHeight() + padMessages;
            reLayout();
        }

        if(autoScroll || isAtBottom()) autoScroll = true;
        if(!autoScroll) unreadMessages++;

        totalHeight += message.getTotalHeight() + padMessages;

        // IMPORTANT: add height to scrollOffset so visually nothing moves
        scrollOffset -= message.getTotalHeight() + padMessages;
    }

    public void setPad(float left, float right, float top, float bottom, float messages) {
        padLeft = left;
        padRight = right;
        padTop = top;
        padBottom = bottom;
        padMessages = messages;
    }

    public int unreadMessages() {
        return unreadMessages;
    }



    private void reLayout() {
        // invalidate messages layout
        for (int i = 0; i < messageProvider.size(); i++) {
            messageProvider.getMessage(i).invalidateLayout();
        }
    }

    private void clampScroll() {
        float max = Math.max(0, totalHeight - getHeight() + padTop + padBottom);
        scrollOffset = MathUtil.clamp(scrollOffset, -max, 0);
    }

    private boolean isAtBottom() {
        return Math.abs(scrollOffset) < AUTO_SCROLL_THRESHOLD;
    }

    @Override
    public void act(float delta) {
        scrollVelocity = MathUtil.clamp(scrollVelocity, -MAX_SCROLL_SPEED, MAX_SCROLL_SPEED); // clamp scroll speed
        scrollOffset += scrollVelocity * delta; // apply scroll speed
        if(autoScroll) scrollOffset += AUTO_SCROLL_SPEED * delta; // apply smooth auto scrolling
        clampScroll(); // clamp scrollOffset

        if(isAtBottom()) unreadMessages = 0;

        // decelerate
        scrollVelocity *= (float) Math.pow(0.90f, delta * 70f);
        if(Math.abs(scrollVelocity) < 1f) scrollVelocity = 0;

        for (int i = 0; i < messageProvider.size(); i++) {
            ChatMessage msg = messageProvider.getMessage(i);
            layout(UI.getFontManager().getFont(FontManager.Fonts.MARTIAN, fontSize, false), msg, getWidth() - padLeft - padRight);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        BitmapFont font = UI.getFontManager().getFont(FontManager.Fonts.MARTIAN, fontSize, false);
        currentColor = -1;

        Drawable background = UI.skin().getDrawable("default-pane-noborder");

        batch.setColor(new Color(0x0E0B0AC0)); // 0x0F1115B0
        background.draw(batch, getX(), getY(), getWidth(), getHeight());
        batch.setColor(new Color(0xD94A3A2a)); // #D94A3A (red-ish)
        background.draw(batch, getX(), getY(), getWidth(), 1); // bottom left-right
        background.draw(batch, getX(), getY(), 1, getHeight()); // left bottom-top
        background.draw(batch, getX(), getTop()-1, getWidth(), 1); // top left-right
        background.draw(batch, getRight()-1, getY(), 1, getHeight()); // right bottom-top
        batch.setColor(1,1,1,1);

        batch.flush();

        tmpVec.set(padLeft, padBottom);
        localToStageCoordinates(tmpVec);

        bounds.set(tmpVec.x, tmpVec.y, getWidth() - padLeft - padRight, getHeight() - padTop - padBottom);
        ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), bounds, scissors);

        if (ScissorStack.pushScissors(scissors)) {
            Color pre = font.getColor();
            font.setColor(1f,1f,1f,1f);

            drawMessages(batch, font, getX(), getY(), getWidth(), getHeight());

            drawUnreadMessages(batch);

            // restore render context
            batch.flush();
            ScissorStack.popScissors();
            font.setColor(pre);
        }
    }

    private final GlyphLayout glyph = new GlyphLayout();

    private void drawUnreadMessages(Batch batch) {
        if(unreadMessages == 0) return;

        BitmapFont font = UI.getFontManager().getFont(22, true);
        String text = unreadMessages < 100 ? "+" + unreadMessages : "+99";

        font.setColor(NOTIFY_COLOR);
        glyph.setText(font, text);
        font.draw(batch, glyph, getRight() - 12 - glyph.width, getY() + 5 + glyph.height);
    }

    private void drawMessages(Batch batch, BitmapFont font, float xRaw, float yRaw, float widthRaw, float heightRaw) {
        float x = xRaw + padLeft;
        float y = yRaw + padTop;
        float width = widthRaw - padLeft - padRight;
        float height = heightRaw - padTop - padBottom;

        float drawY = y + scrollOffset;

        for (int i = messageProvider.size() - 1; i >= 0; i--) {
            ChatMessage msg = messageProvider.getMessage(i);

            for (int l = msg.getLines().size() - 1; l >= 0; l--) {
                ChatMessage.ChatLine line = msg.getLines().get(l);
                float h = font.getLineHeight();
                if (drawY + h < y) {
                    drawY += h;
                    continue;
                }
                if (drawY > y + height) break;

                drawLine(batch, font, line, x, drawY + h);

                drawY += h;
            }
            drawY += padMessages;
        }
    }

    private final Color color = new Color(1,1,1,1);
    private int currentColor = -1;

    private void drawLine(Batch batch, BitmapFont font, ChatMessage.ChatLine line, float x, float y) {
        float drawX = x;

        for(ChatMessage.ChatSegment seg : line.segments) {
            if (seg.rgba8888 != currentColor) {
                Color.rgba8888ToColor(color, seg.rgba8888);
                font.setColor(color);
                currentColor = seg.rgba8888;
            }

            glyph.setText(font, seg.text);
            font.draw(batch, glyph, drawX, y);

            drawX += glyph.width;
        }
    }

    private void layout(BitmapFont font, ChatMessage msg, float maxWidth) {
        if(!msg.isDirty()) return;

        totalHeight -= msg.getTotalHeight() + padMessages;
        msg.layout(font, maxWidth);
        totalHeight += msg.getTotalHeight() + padMessages;
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        reLayout();
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();
        reLayout();
    }

    @Override
    public float getPrefWidth() {
        return 100;
    }

    @Override
    public float getPrefHeight() {
        return 100;
    }

}
