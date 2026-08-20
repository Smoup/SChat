package hw.smoup.schat.client.chat;

import hw.smoup.schat.client.chat.ChatOverlay.RectSink;
import hw.smoup.schat.client.chat.ChatOverlay.TextSink;
import hw.smoup.schat.client.config.ChatPanel;
import hw.smoup.schat.client.config.ChatTab;
import hw.smoup.schat.client.config.SchatConfig;
import net.minecraft.client.gui.Font;

public final class TabDrag {

    private static final int THRESHOLD = 3;
    private static final int GHOST_BACKGROUND = 0xDD404040;
    private static final int GHOST_BORDER = 0xFFFFC94D;
    private static final int GHOST_HIGHLIGHT = 0x66FFC94D;

    private static boolean leftDown;
    private static ChatPanel panel;
    private static int index = TabStrip.NOTHING;
    private static boolean dragging;
    private static int pressX;
    private static int pressY;
    private static int grabX;
    private static int grabY;

    private TabDrag() {
    }

    public static void onMouseButton(int button, int action) {
        if (button == 0) {
            leftDown = action != 0;
        }
    }

    public static void begin(ChatPanel source, int tabIndex, int mouseX, int mouseY) {
        panel = source;
        index = tabIndex;
        dragging = false;
        pressX = mouseX;
        pressY = mouseY;
        ChatFrame frame = ChatFrame.of(source, true);
        grabX = mouseX - TabStrip.tabLeft(source, true, tabIndex);
        grabY = mouseY - TabStrip.stripTop(source, frame);
    }

    public static boolean dragging() {
        return dragging && panel != null;
    }

    public static void update(int mouseX, int mouseY) {
        if (panel == null) {
            return;
        }
        if (leftDown) {
            startIfMoved(mouseX, mouseY);
            return;
        }
        if (dragging) {
            drop(mouseX, mouseY);
        } else {
            ChatTabs.select(panel, index);
        }
        panel = null;
        index = TabStrip.NOTHING;
        dragging = false;
    }

    private static void startIfMoved(int mouseX, int mouseY) {
        boolean moved = Math.abs(mouseX - pressX) > THRESHOLD
                || Math.abs(mouseY - pressY) > THRESHOLD;
        dragging = moved && ChatTabs.canRemoveTab(panel);
    }

    private static void drop(int mouseX, int mouseY) {
        SchatConfig config = SchatConfig.get();
        ChatPanel target = TabStrip.panelAt(mouseX, mouseY);
        ChatPanel source = panel;
        if (target == source) {
            reorder(source, mouseX, mouseY);
            return;
        }
        if (!ChatTabs.canRemoveTab(source)) {
            ChatTabs.select(source, index);
            return;
        }
        ChatTab tab = source.takeTab(index);
        if (tab == null) {
            return;
        }
        boolean created = target == null;
        if (created) {
            target = config.addPanel(source, mouseX, mouseY);
        }
        target.addTab(tab);
        ChatTabs.select(target, target.tabs().size() - 1);
        if (created) {
            alignToCursor(target, mouseX, mouseY);
        }
        if (source.empty()) {
            config.removePanel(source);
        } else {
            ChatTabs.rebuild(source);
        }
        config.save();
    }

    // Новая панель встаёт так, чтобы вкладка осталась ровно под курсором: иначе она
    // прыгает к нему нижним левым углом.
    private static void alignToCursor(ChatPanel created, int mouseX, int mouseY) {
        ChatFrame frame = ChatFrame.of(created, true);
        int deltaX = mouseX - grabX - TabStrip.tabLeft(created, true, 0);
        int deltaY = mouseY - grabY - TabStrip.stripTop(created, frame);
        created.setOffset(created.offsetX() + deltaX, created.offsetY() + deltaY);
    }

    // Бросок на свою же полосу меняет порядок вкладок, а не переносит их куда-то.
    private static void reorder(ChatPanel source, int mouseX, int mouseY) {
        int dropIndex = TabStrip.hitTest(source, true, mouseX, mouseY);
        if (dropIndex < 0 || dropIndex == index) {
            ChatTabs.select(source, index);
            return;
        }
        ChatTab moved = source.takeTab(index);
        if (moved == null) {
            return;
        }
        int insert = Math.min(dropIndex, source.tabs().size());
        source.insertTab(insert, moved);
        ChatTabs.select(source, insert);
    }

    public static void draw(RectSink rects, TextSink texts, Font font, int mouseX, int mouseY) {
        if (!dragging() || index < 0 || index >= panel.tabs().size()) {
            return;
        }
        ChatTab tab = panel.tabs().get(index);
        ChatPanel target = TabStrip.panelAt(mouseX, mouseY);
        if (target != null && target != panel) {
            ChatFrame frame = ChatFrame.of(target, true);
            rects.fill(frame.left(), frame.top(), frame.right(), frame.bottom(), GHOST_HIGHLIGHT);
            drawTab(rects, texts, font, tab, mouseX, mouseY);
            return;
        }
        ChatFrame source = ChatFrame.of(panel, true);
        int width = source.right() - source.left();
        int height = source.bottom() - source.top();
        int left = mouseX - grabX;
        int stripTop = mouseY - grabY;
        int top = panel.tabsBelow()
                ? stripTop - TabStrip.HEIGHT_HANDLE_GAP - height
                : stripTop + TabStrip.RESERVED;
        outline(rects, left, top, left + width, top + height);
        drawTab(rects, texts, font, tab, left, stripTop + TabStrip.HEIGHT / 2);
    }

    private static void drawTab(RectSink rects, TextSink texts, Font font, ChatTab tab,
                                int left, int centerY) {
        int width = TabStrip.tabWidth(font, tab);
        int top = centerY - TabStrip.HEIGHT / 2;
        rects.fill(left, top, left + width, top + TabStrip.HEIGHT, GHOST_BACKGROUND);
        texts.draw(font, tab.name(), left + TabStrip.PADDING, top + 2, TabStrip.NAME_ACTIVE);
    }

    private static void outline(RectSink rects, int left, int top, int right, int bottom) {
        rects.fill(left, top, right, top + 1, GHOST_BORDER);
        rects.fill(left, bottom - 1, right, bottom, GHOST_BORDER);
        rects.fill(left, top, left + 1, bottom, GHOST_BORDER);
        rects.fill(right - 1, top, right, bottom, GHOST_BORDER);
    }
}
