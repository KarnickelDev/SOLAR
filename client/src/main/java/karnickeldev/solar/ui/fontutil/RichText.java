package karnickeldev.solar.ui.fontutil;

/**
 * @author KarnickelDev
 * @since 25.05.2026
 **/
public record RichText(TextRun[] runs, int length) {

    public boolean isEmpty() {
        return runs == null || runs.length == 0;
    }

    public String getRawText() {
        StringBuilder sb = new StringBuilder();
        for (TextRun run : runs) sb.append(run.text);
        return sb.toString();
    }

    public boolean equals(RichText other) {
        if(length != other.length || runs.length != other.runs.length) return false;

        for(int i = 0; i < runs.length; i++) {
            if(!runs[i].isSameStyle(other.runs[i])) return false;
            if(!runs[i].text.equals(other.runs[i].text)) return false;
        }

        return true;
    }

    public static RichText empty() {
        return new RichText(new TextRun[]{new TextRun("", 0xFFFFFFFF)}, 0);
    }

    public static RichText of(String text) {
        return new RichText(new TextRun[]{new TextRun(text, 0xFFFFFFFF, 0)}, text.length());
    }

}
