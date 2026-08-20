package hw.smoup.schat.client.chat;

import hw.smoup.schat.client.Translations;
import hw.smoup.schat.client.chat.ChatOverlay.RectSink;
import hw.smoup.schat.client.chat.ChatOverlay.TextSink;
import hw.smoup.schat.client.config.ChatPanel;
import hw.smoup.schat.client.config.ChatTab;
import hw.smoup.schat.client.config.SchatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.List;

public final class TabStrip {

    public static final int HEIGHT = 12;
    public static final int HEIGHT_HANDLE_GAP = 5;
    public static final int RESERVED = HEIGHT + HEIGHT_HANDLE_GAP;

    public static final int ADD_BUTTON = -2;
    public static final int SETTINGS_BUTTON = -3;
    public static final int NOTHING = -1;

    static final int PADDING = 4;
    static final int NAME_ACTIVE = 0xFFFFFFFF;

    private static final int BADGE_PADDING = 2;
    private static final int ADD_WIDTH = 11;
    private static final int SETTINGS_WIDTH = 12;

    private static final int TAB_ACTIVE = 0xDD202020;
    private static final int TAB_IDLE = 0x99101010;
    private static final int TAB_HOVER = 0xCC303030;
    private static final int TAB_UNDERLINE = 0xFFFFFFFF;
    private static final int NAME_IDLE = 0xFFA0A0A0;
    private static final int BADGE_BORDER = 0xFFFF4444;
    private static final int BADGE_TEXT = 0xFFFFFFFF;
    private static final int ADD_COLOR = 0xFFB0B0B0;
    private static final int HINT_BACKGROUND = 0xE0101010;

    private TabStrip() {
    }

    public static void onMouseButton(int button, int action) {
        TabDrag.onMouseButton(button, action);
    }

    public static void renderAll(RectSink rects, TextSink texts, boolean chatFocused,
                                 int mouseX, int mouseY) {
        TabDrag.update(mouseX, mouseY);
        Font font = font();
        if (font == null) {
            return;
        }
        String hint = null;
        for (ChatPanel panel : SchatConfig.get().panels()) {
            String panelHint = renderPanel(rects, texts, font, panel, chatFocused, mouseX, mouseY);
            if (panelHint != null) {
                hint = panelHint;
            }
        }
        if (TabDrag.dragging()) {
            TabDrag.draw(rects, texts, font, mouseX, mouseY);
        } else if (hint != null) {
            drawHint(rects, texts, font, hint, mouseX, mouseY);
        }
    }

    public static boolean click(double mouseX, double mouseY, int button) {
        for (ChatPanel panel : SchatConfig.get().panels()) {
            int index = hitTest(panel, true, mouseX, mouseY);
            if (index != NOTHING) {
                handleClick(panel, index, button, (int) mouseX, (int) mouseY);
                return true;
            }
        }
        return false;
    }

    public static ChatPanel panelAt(double mouseX, double mouseY) {
        for (ChatPanel panel : SchatConfig.get().panels()) {
            if (panel.empty()) {
                continue;
            }
            ChatFrame frame = ChatFrame.of(panel, true);
            int top = Math.min(frame.top(), stripTop(panel, frame));
            int bottom = Math.max(frame.bottom(), stripTop(panel, frame) + HEIGHT);
            if (mouseX >= frame.left() && mouseX <= frame.right()
                    && mouseY >= top && mouseY <= bottom) {
                return panel;
            }
        }
        return null;
    }

    public static boolean visible(boolean chatFocused) {
        return !chatHidden() && (chatFocused || ChatTabs.anyUnread());
    }

    public static int hitTest(ChatPanel panel, boolean chatFocused, double mouseX, double mouseY) {
        Font font = font();
        if (font == null || panel.empty() || !visible(chatFocused)) {
            return NOTHING;
        }
        int top = stripTop(panel, chatFocused);
        if (mouseY < top || mouseY >= top + HEIGHT) {
            return NOTHING;
        }
        ChatFrame frame = ChatFrame.of(panel, chatFocused);
        if (chatFocused && mouseX >= frame.right() - SETTINGS_WIDTH && mouseX < frame.right()) {
            return SETTINGS_BUTTON;
        }
        List<ChatTab> tabs = panel.tabs();
        int x = frame.left();
        for (int index = 0; index < tabs.size(); index++) {
            if (!ChatTabs.availableHere(tabs.get(index))) {
                continue;
            }
            int width = tabWidth(font, tabs.get(index));
            if (mouseX >= x && mouseX < x + width) {
                return index;
            }
            x += width + 1;
        }
        if (chatFocused && mouseX >= x && mouseX < x + ADD_WIDTH) {
            return ADD_BUTTON;
        }
        return NOTHING;
    }

    static int tabLeft(ChatPanel panel, boolean chatFocused, int index) {
        Font font = font();
        int x = ChatFrame.of(panel, chatFocused).left();
        if (font == null) {
            return x;
        }
        List<ChatTab> tabs = panel.tabs();
        for (int current = 0; current < index && current < tabs.size(); current++) {
            if (ChatTabs.availableHere(tabs.get(current))) {
                x += tabWidth(font, tabs.get(current)) + 1;
            }
        }
        return x;
    }

    static int tabWidth(Font font, ChatTab tab) {
        int width = PADDING + font.width(tab.name()) + PADDING;
        if (tab.unread() > 0) {
            width += badgeWidth(font, String.valueOf(tab.unread())) + PADDING;
        }
        return width;
    }

    private static String renderPanel(RectSink rects, TextSink texts, Font font, ChatPanel panel,
                                      boolean chatFocused, int mouseX, int mouseY) {
        if (panel.empty() || !visible(chatFocused)) {
            return null;
        }
        ChatFrame frame = ChatFrame.of(panel, chatFocused);
        int top = stripTop(panel, frame);
        boolean onStrip = chatFocused && mouseY >= top && mouseY < top + HEIGHT;
        String hint = null;

        int x = frame.left();
        List<ChatTab> tabs = panel.tabs();
        for (int index = 0; index < tabs.size(); index++) {
            ChatTab tab = tabs.get(index);
            if (!ChatTabs.availableHere(tab)) {
                continue;
            }
            int width = tabWidth(font, tab);
            boolean hovered = onStrip && mouseX >= x && mouseX < x + width;
            drawTab(rects, texts, font, tab, x, top, index == panel.activeIndex(), hovered);
            if (hovered) {
                hint = Translations.get(ChatTabs.canRemoveTab(panel)
                        ? "schat.tab.hint"
                        : "schat.tab.hint.last");
            }
            x += width + 1;
        }

        if (!chatFocused) {
            return hint;
        }
        boolean onAdd = onStrip && mouseX >= x && mouseX < x + ADD_WIDTH;
        drawAddButton(rects, texts, font, x, top, onAdd);
        if (onAdd) {
            hint = Translations.get("schat.tab.hint.add");
        }
        int settingsLeft = frame.right() - SETTINGS_WIDTH;
        boolean onSettings = onStrip && mouseX >= settingsLeft && mouseX < frame.right();
        drawSettingsButton(rects, settingsLeft, frame.right(), top, onSettings);
        return onSettings ? Translations.get("schat.tab.hint.settings") : hint;
    }

    private static void drawTab(RectSink rects, TextSink texts, Font font, ChatTab tab,
                                int left, int top, boolean active, boolean hovered) {
        String badge = tab.unread() > 0 ? String.valueOf(tab.unread()) : null;
        int nameWidth = font.width(tab.name());
        int badgeWidth = badge == null ? 0 : badgeWidth(font, badge);
        int width = PADDING + nameWidth + PADDING + (badge == null ? 0 : badgeWidth + PADDING);
        int bottom = top + HEIGHT;

        rects.fill(left, top, left + width, bottom, tab.tabBackground(active || hovered));
        if (active) {
            rects.fill(left, bottom - 1, left + width, bottom, TAB_UNDERLINE);
        }
        texts.draw(font, tab.name(), left + PADDING, top + 2, tab.tabForeground(active || hovered));
        if (badge != null) {
            drawBadge(rects, texts, font, badge, badgeWidth,
                    left + PADDING + nameWidth + PADDING, top);
        }
    }

    private static void drawAddButton(RectSink rects, TextSink texts, Font font, int left, int top,
                                      boolean hovered) {
        rects.fill(left, top, left + ADD_WIDTH, top + HEIGHT, hovered ? TAB_HOVER : TAB_IDLE);
        texts.draw(font, "+", left + 4, top + 2, ADD_COLOR);
    }

    private static void drawSettingsButton(RectSink rects, int left, int right, int top,
                                           boolean hovered) {
        rects.fill(left, top, right, top + HEIGHT, hovered ? TAB_HOVER : TAB_IDLE);
        int color = hovered ? NAME_ACTIVE : ADD_COLOR;
        for (int line = 0; line < 3; line++) {
            int y = top + 3 + line * 3;
            rects.fill(left + 2, y, right - 2, y + 1, color);
        }
    }

    private static void drawBadge(RectSink rects, TextSink texts, Font font, String text,
                                  int width, int left, int top) {
        int y = top + 1;
        int height = HEIGHT - 2;
        rects.fill(left, y, left + width, y + 1, BADGE_BORDER);
        rects.fill(left, y + height - 1, left + width, y + height, BADGE_BORDER);
        rects.fill(left, y, left + 1, y + height, BADGE_BORDER);
        rects.fill(left + width - 1, y, left + width, y + height, BADGE_BORDER);
        texts.draw(font, text, left + BADGE_PADDING + 1, y + 1, BADGE_TEXT);
    }

    private static void drawHint(RectSink rects, TextSink texts, Font font, String hint,
                                 int mouseX, int mouseY) {
        int width = font.width(hint);
        int x = Math.max(2, Math.min(mouseX + 8, ChatFrame.guiWidth() - width - 4));
        int y = Math.max(2, mouseY - 14);
        rects.fill(x - 2, y - 2, x + width + 2, y + 10, HINT_BACKGROUND);
        texts.draw(font, hint, x, y, NAME_ACTIVE);
    }

    private static void handleClick(ChatPanel panel, int index, int button, int mouseX, int mouseY) {
        if (index == ADD_BUTTON) {
            if (button == 0) {
                ChatTabs.addTab(panel);
            }
            return;
        }
        if (index == SETTINGS_BUTTON) {
            if (button == 0) {
                TabSettingsScreen.open(panel);
            }
            return;
        }
        if (button == 2) {
            ChatTabs.removeTab(panel, index);
        } else if (button == 1) {
            TabSettingsScreen.open(panel, panel.tabs().get(index));
        } else if (button == 0) {
            TabDrag.begin(panel, index, mouseX, mouseY);
        }
    }

    private static int badgeWidth(Font font, String badge) {
        return font.width(badge) + BADGE_PADDING * 2 + 2;
    }

    private static int stripTop(ChatPanel panel, boolean chatFocused) {
        return stripTop(panel, ChatFrame.of(panel, chatFocused));
    }

    static int stripTop(ChatPanel panel, ChatFrame frame) {
        return panel.tabsBelow() ? frame.bottom() + HEIGHT_HANDLE_GAP : frame.top() - RESERVED;
    }

    private static boolean chatHidden() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null || minecraft.options == null
                || minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
    }

    private static Font font() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.font;
    }
}
