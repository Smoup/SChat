package hw.smoup.schat.client.config;

import hw.smoup.schat.client.chat.ChatFrame;
import hw.smoup.schat.client.chat.PanelBound;
import hw.smoup.schat.client.chat.TabStrip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;

import java.util.ArrayList;
import java.util.List;

public final class ChatPanel {

    public static final int MIN_WIDTH = 40;
    public static final int MIN_HEIGHT = 20;
    public static final double MIN_SCALE = 0.25;
    public static final double MAX_SCALE = 3.0;

    public static final int DEFAULT_WIDTH = 320;
    public static final int DEFAULT_HEIGHT_FOCUSED = 180;
    public static final int DEFAULT_HEIGHT_UNFOCUSED = 90;
    public static final double DEFAULT_SCALE = 1.0;

    private static final int CHAT_INPUT_HEIGHT = 14;

    private int width = DEFAULT_WIDTH;
    private double scale = DEFAULT_SCALE;
    private int heightFocused = DEFAULT_HEIGHT_FOCUSED;
    private int heightUnfocused = DEFAULT_HEIGHT_UNFOCUSED;
    private int offsetX;
    private int offsetY;

    private int defaultWidth = DEFAULT_WIDTH;
    private double defaultScale = DEFAULT_SCALE;
    private int defaultHeightFocused = DEFAULT_HEIGHT_FOCUSED;
    private int defaultHeightUnfocused = DEFAULT_HEIGHT_UNFOCUSED;
    private int defaultOffsetX;
    private int defaultOffsetY;

    private List<ChatTab> tabs = new ArrayList<>();
    private int activeTab;

    private final transient ScreenFit fit = new ScreenFit();
    private final transient RepeatState repeat = new RepeatState();
    private transient boolean primary;
    private transient ChatComponent component;

    public boolean primary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public ChatComponent component() {
        if (component != null) {
            return component;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return null;
        }
        bind(primary ? minecraft.gui.getChat() : new ChatComponent(minecraft));
        return component;
    }

    public void bind(ChatComponent component) {
        this.component = component;
        ((PanelBound) component).schat$setPanel(this);
    }

    public String lastShownText() {
        return repeat.text;
    }

    public int lastShownCount() {
        return repeat.count;
    }

    public void setLastShown(String text, int count) {
        repeat.text = text;
        repeat.count = count;
    }

    public int lastLines() {
        return repeat.lines;
    }

    public void setLastLines(int lines) {
        repeat.lines = lines;
    }

    public int width() {
        return width;
    }

    public double scale() {
        return scale;
    }

    public int height(boolean chatFocused) {
        return chatFocused ? heightFocused : heightUnfocused;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }

    public void setWidth(int value) {
        width = Math.max(MIN_WIDTH, value);
    }

    public void setScale(double value) {
        scale = Math.min(MAX_SCALE, Math.max(MIN_SCALE, value));
    }

    public void setHeight(boolean chatFocused, int value) {
        int height = Math.max(MIN_HEIGHT, value);
        if (chatFocused) {
            heightFocused = height;
        } else {
            heightUnfocused = height;
        }
    }

    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setPositionFromScreen(int screenLeft, int screenBottom) {
        setOffset(screenLeft, screenBottom - ChatFrame.baseBottom(scale));
    }

    public int effectiveWidth() {
        fit.refresh();
        return fit.width;
    }

    public int effectiveHeight(boolean chatFocused) {
        fit.refresh();
        return chatFocused ? fit.heightFocused : fit.heightUnfocused;
    }

    public int effectiveOffsetX() {
        fit.refresh();
        return fit.offsetX;
    }

    public int effectiveOffsetY() {
        fit.refresh();
        return fit.offsetY;
    }

    public int defaultWidth() {
        return defaultWidth;
    }

    public double defaultScale() {
        return defaultScale;
    }

    public int defaultHeight(boolean chatFocused) {
        return chatFocused ? defaultHeightFocused : defaultHeightUnfocused;
    }

    public int defaultOffsetX() {
        return defaultOffsetX;
    }

    public int defaultOffsetY() {
        return defaultOffsetY;
    }

    public void rememberWidth() {
        defaultWidth = width;
    }

    public void rememberScale() {
        defaultScale = scale;
    }

    public void rememberHeight(boolean chatFocused) {
        if (chatFocused) {
            defaultHeightFocused = heightFocused;
        } else {
            defaultHeightUnfocused = heightUnfocused;
        }
    }

    public void rememberOffset() {
        defaultOffsetX = offsetX;
        defaultOffsetY = offsetY;
    }

    public ChatPanel copyGeometry() {
        ChatPanel copy = new ChatPanel();
        copy.width = width;
        copy.scale = scale;
        copy.heightFocused = heightFocused;
        copy.heightUnfocused = heightUnfocused;
        copy.defaultWidth = defaultWidth;
        copy.defaultScale = defaultScale;
        copy.defaultHeightFocused = defaultHeightFocused;
        copy.defaultHeightUnfocused = defaultHeightUnfocused;
        return copy;
    }

    public List<ChatTab> tabs() {
        return tabs;
    }

    public ChatTab activeTab() {
        return tabs.get(activeTab);
    }

    public int activeIndex() {
        return activeTab;
    }

    public void setActiveIndex(int index) {
        activeTab = tabs.isEmpty() ? 0 : Math.min(Math.max(index, 0), tabs.size() - 1);
    }

    public void addTab(ChatTab tab) {
        tabs.add(tab);
    }

    public boolean removeTab(int index) {
        if (tabs.size() <= 1 || index < 0 || index >= tabs.size()) {
            return false;
        }
        tabs.remove(index);
        setActiveIndex(activeTab > index ? activeTab - 1 : activeTab);
        return true;
    }

    public ChatTab takeTab(int index) {
        if (index < 0 || index >= tabs.size()) {
            return null;
        }
        ChatTab tab = tabs.remove(index);
        setActiveIndex(activeTab > index ? activeTab - 1 : activeTab);
        return tab;
    }

    public boolean empty() {
        return tabs.isEmpty();
    }

    public int historyLimit() {
        int limit = ChatTab.MIN_HISTORY;
        for (ChatTab tab : tabs) {
            limit = Math.max(limit, tab.historyLimit());
        }
        return limit;
    }

    void sanitize(String defaultTabName) {
        if (tabs == null) {
            tabs = new ArrayList<>();
        }
        tabs.removeIf(tab -> tab == null);
        for (ChatTab tab : tabs) {
            tab.sanitize();
        }
        if (tabs.isEmpty()) {
            tabs.add(ChatTab.named(defaultTabName));
        }
        setActiveIndex(activeTab);
        setWidth(width);
        setScale(scale);
        setHeight(true, heightFocused);
        setHeight(false, heightUnfocused);
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    private static final class RepeatState {

        private String text;
        private int count;
        private int lines;
    }

    // Хранимые размеры остаются такими, какими их задал игрок: под экран они лишь
    // подгоняются, иначе один кадр с маленьким окном переписал бы конфиг навсегда.
    private final class ScreenFit {

        private int guiWidth = -1;
        private int guiHeight = -1;
        private int sourceWidth;
        private int sourceHeightFocused;
        private int sourceHeightUnfocused;
        private int sourceOffsetX;
        private int sourceOffsetY;
        private double sourceScale;

        private int width;
        private int heightFocused;
        private int heightUnfocused;
        private int offsetX;
        private int offsetY;

        private void refresh() {
            if (unchanged()) {
                return;
            }
            remember();
            fitSize();
            fitOffset();
        }

        private boolean unchanged() {
            return guiWidth == ChatFrame.guiWidth()
                    && guiHeight == ChatFrame.guiHeight()
                    && sourceWidth == ChatPanel.this.width
                    && sourceHeightFocused == ChatPanel.this.heightFocused
                    && sourceHeightUnfocused == ChatPanel.this.heightUnfocused
                    && sourceOffsetX == ChatPanel.this.offsetX
                    && sourceOffsetY == ChatPanel.this.offsetY
                    && sourceScale == ChatPanel.this.scale;
        }

        private void remember() {
            guiWidth = ChatFrame.guiWidth();
            guiHeight = ChatFrame.guiHeight();
            sourceWidth = ChatPanel.this.width;
            sourceHeightFocused = ChatPanel.this.heightFocused;
            sourceHeightUnfocused = ChatPanel.this.heightUnfocused;
            sourceOffsetX = ChatPanel.this.offsetX;
            sourceOffsetY = ChatPanel.this.offsetY;
            sourceScale = ChatPanel.this.scale;
        }

        private void fitSize() {
            double scale = sourceScale;
            width = clamp(sourceWidth, MIN_WIDTH, Math.max(MIN_WIDTH, ChatFrame.maxWidth(scale)));
            int tabStripInChatUnits = (int) Math.ceil(TabStrip.RESERVED / scale);
            int maxHeight = Math.max(MIN_HEIGHT, ChatFrame.maxHeight(scale) - tabStripInChatUnits);
            heightFocused = clamp(sourceHeightFocused, MIN_HEIGHT, maxHeight);
            heightUnfocused = clamp(sourceHeightUnfocused, MIN_HEIGHT, maxHeight);
        }

        private void fitOffset() {
            if (guiWidth <= 0 || guiHeight <= 0) {
                offsetX = sourceOffsetX;
                offsetY = sourceOffsetY;
                return;
            }
            double scale = sourceScale;
            int bottom = ChatFrame.baseBottom(scale);
            int highestTop = Math.min(ChatFrame.baseTop(scale, heightFocused),
                    ChatFrame.baseTop(scale, heightUnfocused)) - TabStrip.RESERVED;
            offsetX = withinScreen(sourceOffsetX, 0, guiWidth - ChatFrame.baseRight(scale, width));
            offsetY = withinScreen(sourceOffsetY, -highestTop,
                    guiHeight - bottom - CHAT_INPUT_HEIGHT);
        }

        private int withinScreen(int value, int min, int max) {
            return min > max ? 0 : clamp(value, min, max);
        }
    }
}
