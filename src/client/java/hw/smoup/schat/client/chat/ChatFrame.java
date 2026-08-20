package hw.smoup.schat.client.chat;

import hw.smoup.schat.client.config.ChatPanel;
import hw.smoup.schat.client.config.SchatConfig;
import net.minecraft.client.Minecraft;

public record ChatFrame(int left, int top, int right, int bottom) {

    public static final int CHAT_INPUT_HEIGHT = 14;

    private static final int VANILLA_BACKGROUND_PAD = 12;
    private static final int INPUT_GAP = 4;

    public static ChatFrame of(ChatPanel panel, boolean chatFocused) {
        int offsetX = panel.effectiveOffsetX();
        int offsetY = panel.effectiveOffsetY() + stateOffsetY(panel, chatFocused);
        double scale = panel.scale();
        return new ChatFrame(
                offsetX,
                offsetY + baseTop(scale, panel.effectiveHeight(chatFocused)),
                offsetX + baseRight(scale, panel.effectiveWidth()),
                offsetY + baseBottom(scale));
    }

    // Ваниль держит чат в 40 пикселях от низа под строку ввода. Пока чат закрыт, это
    // место пустует, а с открытым чатом низ панели упирается в саму строку. Смотрим на
    // фактическое положение: панель у низа подтягивается, поднятая к центру не трогается.
    public static int stateOffsetY(ChatPanel panel, boolean chatFocused) {
        int guiHeight = guiHeight();
        if (guiHeight <= 0) {
            return 0;
        }
        int tabStrip = panel.tabsBelow() ? TabStrip.RESERVED : 0;
        int bottom = panel.effectiveOffsetY() + baseBottom(panel.scale()) + tabStrip;
        if (chatFocused) {
            return Math.min(0, guiHeight - CHAT_INPUT_HEIGHT - INPUT_GAP - bottom);
        }
        int lowestZone = guiHeight - CHAT_INPUT_HEIGHT - SchatConfig.BOTTOM_MARGIN;
        return bottom >= lowestZone ? guiHeight - bottom : 0;
    }

    public static int baseRight(double scale, int width) {
        return round(chatUnits(width, scale) * scale + VANILLA_BACKGROUND_PAD * scale);
    }

    public static int baseTop(double scale, int height) {
        return round((bottomLine(scale) - height) * scale);
    }

    public static int baseBottom(double scale) {
        return round(bottomLine(scale) * scale);
    }

    public static int widthFromMouse(ChatPanel panel, double mouseX) {
        return round(mouseX - panel.effectiveOffsetX() - VANILLA_BACKGROUND_PAD * panel.scale());
    }

    public static int heightFromMouse(ChatPanel panel, boolean chatFocused, double mouseY) {
        double scale = panel.scale();
        int offsetY = panel.effectiveOffsetY() + stateOffsetY(panel, chatFocused);
        return bottomLine(scale) - round((mouseY - offsetY) / scale);
    }

    public static int maxWidth(double scale) {
        return guiWidth() - round(VANILLA_BACKGROUND_PAD * scale);
    }

    public static int maxHeight(double scale) {
        return bottomLine(scale);
    }

    public static int guiWidth() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null || minecraft.getWindow() == null
                ? 0
                : minecraft.getWindow().getGuiScaledWidth();
    }

    public static int guiHeight() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null || minecraft.getWindow() == null
                ? 0
                : minecraft.getWindow().getGuiScaledHeight();
    }

    public boolean contains(double x, double y, int slack) {
        return x >= left - slack && x <= right + slack && y >= top - slack && y <= bottom + slack;
    }

    private static int bottomLine(double scale) {
        return (int) Math.floor((guiHeight() - SchatConfig.BOTTOM_MARGIN) / scale);
    }

    private static double chatUnits(int screenPixels, double scale) {
        return Math.ceil(screenPixels / scale);
    }

    private static int round(double value) {
        return (int) Math.round(value);
    }
}
