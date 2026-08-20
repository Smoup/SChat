package hw.smoup.schat.client.config;

public final class MessageFilter {

    public enum Mode {
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH;

        public Mode next() {
            Mode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private Mode mode = Mode.CONTAINS;
    private String text = "";
    private boolean negate;

    private transient String lowered = "";

    private MessageFilter() {
    }

    public static MessageFilter empty() {
        return new MessageFilter();
    }

    public MessageFilter copy() {
        MessageFilter copy = new MessageFilter();
        copy.mode = mode;
        copy.text = text;
        copy.negate = negate;
        return copy;
    }

    public Mode mode() {
        return mode;
    }

    public void cycle() {
        if (!negate) {
            negate = true;
            return;
        }
        negate = false;
        mode = mode.next();
    }

    public String labelKey() {
        return "schat.filter." + (negate ? "not." : "")
                + mode.name().toLowerCase(java.util.Locale.ROOT);
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
        lowered = this.text.toLowerCase(java.util.Locale.ROOT);
    }

    public boolean negate() {
        return negate;
    }

    public boolean blank() {
        return text.isEmpty();
    }

    public boolean matches(String plainLowerCase) {
        boolean hit = switch (mode) {
            case CONTAINS -> plainLowerCase.contains(lowered);
            case STARTS_WITH -> plainLowerCase.startsWith(lowered);
            case ENDS_WITH -> plainLowerCase.endsWith(lowered);
        };
        return negate != hit;
    }

    void sanitize() {
        if (mode == null) {
            mode = Mode.CONTAINS;
        }
        setText(text);
    }
}
