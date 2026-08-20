package hw.smoup.schat.client.chat;

public enum Handle {
    WIDTH,
    HEIGHT,
    CORNER,
    SCALE,
    MOVE;

    public boolean affectsWidth() {
        return this == WIDTH || this == CORNER;
    }

    public boolean affectsHeight() {
        return this == HEIGHT || this == CORNER;
    }
}
