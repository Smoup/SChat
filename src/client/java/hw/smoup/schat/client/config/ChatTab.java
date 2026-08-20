package hw.smoup.schat.client.config;

import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ChatTab {

    public static final int MIN_HISTORY = 20;
    public static final int MAX_HISTORY = 1000;
    public static final int DEFAULT_HISTORY = 100;

    public static final String DEFAULT_STACK_FORMAT = "(x%s)";
    public static final ChatFormatting DEFAULT_STACK_COLOR = ChatFormatting.GRAY;
    public static final String DEFAULT_NICK_FORMAT = "[⧉]";
    public static final ChatFormatting DEFAULT_NICK_COLOR = ChatFormatting.GREEN;
    public static final String DEFAULT_COPY_FORMAT = "[Copy]";
    public static final ChatFormatting DEFAULT_COPY_COLOR = ChatFormatting.DARK_AQUA;
    public static final ChatFormatting DEFAULT_TAB_COLOR = ChatFormatting.BLACK;
    public static final ChatFormatting DEFAULT_TAB_TEXT_COLOR = ChatFormatting.WHITE;
    public static final double DEFAULT_TAB_OPACITY = 0.75;

    private String name = "";
    private int historyLimit = DEFAULT_HISTORY;
    private Double backgroundOpacity;
    private boolean exclusive;
    private boolean matchAllFilters;
    private boolean showUnread = true;
    private String tabColor = DEFAULT_TAB_COLOR.name();
    private String tabTextColor = DEFAULT_TAB_TEXT_COLOR.name();
    private double tabOpacity = DEFAULT_TAB_OPACITY;
    private boolean nickButton;
    private String nickFormat = DEFAULT_NICK_FORMAT;
    private String nickColor = DEFAULT_NICK_COLOR.name();
    private boolean copyButton;
    private String copyFormat = DEFAULT_COPY_FORMAT;
    private String copyColor = DEFAULT_COPY_COLOR.name();
    private boolean stackEnabled = true;
    private String stackFormat = DEFAULT_STACK_FORMAT;
    private String stackColor = DEFAULT_STACK_COLOR.name();
    private String servers = "";
    private List<MessageFilter> filters = new ArrayList<>();

    private transient int unread;

    private ChatTab() {
    }

    public static ChatTab named(String name) {
        ChatTab tab = new ChatTab();
        tab.name = name;
        return tab;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    public int historyLimit() {
        return historyLimit;
    }

    public void setHistoryLimit(int limit) {
        historyLimit = Math.min(MAX_HISTORY, Math.max(MIN_HISTORY, limit));
    }

    public Double backgroundOpacity() {
        return backgroundOpacity;
    }

    public void setBackgroundOpacity(Double opacity) {
        backgroundOpacity = opacity == null ? null : Math.min(1.0, Math.max(0.0, opacity));
    }

    public void copyLookFrom(ChatTab other) {
        setHistoryLimit(other.historyLimit());
        setBackgroundOpacity(other.backgroundOpacity());
        setTabColor(other.tabColor());
        setTabTextColor(other.tabTextColor());
        setTabOpacity(other.tabOpacity());
        setShowUnread(other.showUnread());
        setStackEnabled(other.stackEnabled());
        setStackFormat(other.stackFormat());
        setStackColor(other.stackColor());
        setNickButton(other.nickButton());
        setNickFormat(other.nickFormat());
        setNickColor(other.nickColor());
        setCopyButton(other.copyButton());
        setCopyFormat(other.copyFormat());
        setCopyColor(other.copyColor());
    }

    public boolean exclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public ChatFormatting tabColor() {
        return color(tabColor, DEFAULT_TAB_COLOR);
    }

    public void setTabColor(ChatFormatting color) {
        tabColor = color == null ? DEFAULT_TAB_COLOR.name() : color.name();
    }

    public ChatFormatting tabTextColor() {
        return color(tabTextColor, DEFAULT_TAB_TEXT_COLOR);
    }

    public void setTabTextColor(ChatFormatting color) {
        tabTextColor = color == null ? DEFAULT_TAB_TEXT_COLOR.name() : color.name();
    }

    public int tabForeground(boolean active) {
        Integer rgb = tabTextColor().getColor();
        int base = rgb == null ? 0xFFFFFF : rgb;
        return ((active ? 0xFF : 0xAA) << 24) | (base & 0xFFFFFF);
    }

    public double tabOpacity() {
        return tabOpacity;
    }

    public void setTabOpacity(double opacity) {
        tabOpacity = Math.min(1.0, Math.max(0.0, opacity));
    }

    // Плашка вкладки: цвет из палитры Minecraft плюс своя прозрачность. Неактивная
    // приглушается, чтобы активная читалась без дополнительной обводки.
    public int tabBackground(boolean active) {
        Integer rgb = tabColor().getColor();
        int base = rgb == null ? 0 : rgb;
        int alpha = (int) Math.round(tabOpacity * 255.0 * (active ? 1.0 : 0.6));
        return (Math.min(255, Math.max(0, alpha)) << 24) | (base & 0xFFFFFF);
    }

    public boolean matchAllFilters() {
        return matchAllFilters;
    }

    public void setMatchAllFilters(boolean matchAllFilters) {
        this.matchAllFilters = matchAllFilters;
    }

    public boolean nickButton() {
        return nickButton;
    }

    public void setNickButton(boolean nickButton) {
        this.nickButton = nickButton;
    }

    public String nickFormat() {
        return nickFormat;
    }

    public void setNickFormat(String format) {
        nickFormat = format == null || format.isEmpty() ? DEFAULT_NICK_FORMAT : format;
    }

    public ChatFormatting nickColor() {
        return color(nickColor, DEFAULT_NICK_COLOR);
    }

    public void setNickColor(ChatFormatting color) {
        nickColor = color == null ? DEFAULT_NICK_COLOR.name() : color.name();
    }

    public boolean copyButton() {
        return copyButton;
    }

    public void setCopyButton(boolean copyButton) {
        this.copyButton = copyButton;
    }

    public String copyFormat() {
        return copyFormat;
    }

    public void setCopyFormat(String format) {
        copyFormat = format == null || format.isEmpty() ? DEFAULT_COPY_FORMAT : format;
    }

    public ChatFormatting copyColor() {
        return color(copyColor, DEFAULT_COPY_COLOR);
    }

    public void setCopyColor(ChatFormatting color) {
        copyColor = color == null ? DEFAULT_COPY_COLOR.name() : color.name();
    }

    public boolean stackEnabled() {
        return stackEnabled;
    }

    public void setStackEnabled(boolean stackEnabled) {
        this.stackEnabled = stackEnabled;
    }

    public String stackFormat() {
        return stackFormat;
    }

    public void setStackFormat(String format) {
        stackFormat = format == null || !hasSingleNumberPlaceholder(format)
                ? DEFAULT_STACK_FORMAT
                : format;
    }

    public ChatFormatting stackColor() {
        return color(stackColor, DEFAULT_STACK_COLOR);
    }

    private static ChatFormatting color(String name, ChatFormatting fallback) {
        try {
            ChatFormatting color = ChatFormatting.valueOf(name);
            return color.isColor() ? color : fallback;
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return fallback;
        }
    }

    public void setStackColor(ChatFormatting color) {
        stackColor = color == null ? DEFAULT_STACK_COLOR.name() : color.name();
    }

    public String servers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers == null ? "" : servers;
    }

    // Пустой список — вкладка живёт везде. Маска «*.holyworld.ru» ловит любой поддомен,
    // сам «holyworld.ru» тоже подходит: играют обычно и по короткому адресу.
    public boolean matchesServer(String address) {
        if (servers.isBlank()) {
            return true;
        }
        if (address == null) {
            return false;
        }
        String host = address.toLowerCase(Locale.ROOT).split(":")[0];
        for (String raw : servers.toLowerCase(Locale.ROOT).split("[,;\\s]+")) {
            String mask = raw.trim();
            if (!mask.isEmpty() && hostMatches(host, mask)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hostMatches(String host, String mask) {
        if (!mask.startsWith("*")) {
            return host.equals(mask);
        }
        String suffix = mask.substring(1);
        if (suffix.startsWith(".")) {
            return host.equals(suffix.substring(1)) || host.endsWith(suffix);
        }
        return host.endsWith(suffix);
    }

    public List<MessageFilter> filters() {
        return filters;
    }

    public void setFilters(List<MessageFilter> filters) {
        this.filters = new ArrayList<>(filters);
    }

    public boolean showUnread() {
        return showUnread;
    }

    public void setShowUnread(boolean showUnread) {
        this.showUnread = showUnread;
    }

    public int unread() {
        return showUnread ? unread : 0;
    }

    public void addUnread() {
        unread++;
    }

    public void markRead() {
        unread = 0;
    }

    public boolean accepts(String plainLowerCase) {
        return breaksNoBan(plainLowerCase) && matchesConditions(plainLowerCase);
    }

    private boolean breaksNoBan(String text) {
        for (MessageFilter filter : filters) {
            if (!filter.blank() && filter.negate() && !filter.matches(text)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesConditions(String text) {
        boolean hasCondition = false;
        for (MessageFilter filter : filters) {
            if (filter.blank() || filter.negate()) {
                continue;
            }
            hasCondition = true;
            boolean hit = filter.matches(text);
            if (matchAllFilters && !hit) {
                return false;
            }
            if (!matchAllFilters && hit) {
                return true;
            }
        }
        return !hasCondition || matchAllFilters;
    }

    private static boolean hasSingleNumberPlaceholder(String format) {
        if (format.replace("%s", "").contains("%")) {
            return false;
        }
        int first = format.indexOf("%s");
        return first >= 0 && format.indexOf("%s", first + 2) < 0;
    }

    void sanitize() {
        setName(name);
        setHistoryLimit(historyLimit);
        setBackgroundOpacity(backgroundOpacity);
        setStackFormat(stackFormat);
        setStackColor(stackColor());
        setNickFormat(nickFormat);
        setNickColor(nickColor());
        setCopyFormat(copyFormat);
        setCopyColor(copyColor());
        if (filters == null) {
            filters = new ArrayList<>();
        }
        filters.removeIf(filter -> filter == null);
        for (MessageFilter filter : filters) {
            filter.sanitize();
        }
    }
}
