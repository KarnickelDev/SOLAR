package karnickeldev.solar.ui.fontutil;

import karnickeldev.solar.ui.layers.hud.chat.ChatMessage;

/**
 * @author KarnickelDev
 * @since 21.04.2026
 **/
public final class MessageWrapper {

    private MessageWrapper() {
    }

    public static void wrap(ChatMessage message, int maxColumns, float fontScale) {

        if (!message.dirty
            && message.wrappedForColumns == maxColumns
            && message.wrappedForScale == fontScale) {
            return;
        }

        message.lineCount = 0;

        int column = 0;

        int lineSegStart = 0;
        int lineCharStart = 0;

        int seg = 0;

        while (seg < message.segmentCount) {

            String text = message.segmentText[seg];
            if (text == null) {
                seg++;
                continue;
            }

            int i = 0;

            while (i < text.length()) {

                int cp = text.codePointAt(i);
                int step = Character.charCount(cp);

                // newline
                if (cp == '\n') {

                    pushLine(message,
                        lineSegStart, lineCharStart,
                        seg, i
                    );

                    column = 0;

                    i += step;

                    // move line start AFTER newline
                    lineSegStart = seg;
                    lineCharStart = i;

                    continue;
                }

                // wrap
                if (maxColumns > 0 && column >= maxColumns) {

                    pushLine(message,
                        lineSegStart, lineCharStart,
                        seg, i
                    );

                    column = 0;

                    lineSegStart = seg;
                    lineCharStart = i;
                }

                column++;
                i += step;
            }

            seg++; // ONLY place seg is allowed to move forward
        }

        // final line
        if (message.segmentCount > 0) {
            int lastSeg = message.segmentCount - 1;
            String lastText = message.segmentText[lastSeg];

            if (lastText != null) {
                pushLine(
                    message,
                    lineSegStart,
                    lineCharStart,
                    lastSeg,
                    lastText.length()
                );
            }
        }

        message.wrappedForColumns = maxColumns;
        message.wrappedForScale = fontScale;
        message.dirty = false;
    }

    private static void pushLine(ChatMessage message,
                                 int segmentStart,
                                 int charStart,
                                 int segmentEnd,
                                 int charEnd) {

        int line = message.lineCount++;
        message.ensureLineCapacity(message.lineCount);

        message.lineSegmentStart[line] = segmentStart;
        message.lineCharStart[line] = charStart;
        message.lineSegmentEnd[line] = segmentEnd;
        message.lineCharEnd[line] = charEnd;
    }
}
