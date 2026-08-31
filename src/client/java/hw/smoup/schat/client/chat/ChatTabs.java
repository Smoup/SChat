package hw.smoup.schat.client.chat;

import hw.smoup.schat.client.Translations;
import hw.smoup.schat.client.config.ChatPanel;
import hw.smoup.schat.client.config.ChatTab;
import hw.smoup.schat.client.config.SchatConfig;
import hw.smoup.schat.mixin.client.ChatComponentAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ChatTabs {

    private static final class Entry {

        private final Component message;
        private final MessageSignature signature;
        private final Object source;
        private final GuiMessageTag tag;
        private final String normalized;

        private long cachedStamp;
        private long cachedMask;

        private Entry(Component message, MessageSignature signature, Object source,
                      GuiMessageTag tag, String normalized) {
            this.message = message;
            this.signature = signature;
            this.source = source;
            this.tag = tag;
            this.normalized = normalized;
        }

        private Component message() {
            return message;
        }

        private MessageSignature signature() {
            return signature;
        }

        private Object source() {
            return source;
        }

        private GuiMessageTag tag() {
            return tag;
        }

        private String normalized() {
            return normalized;
        }
    }

    private static final int TRIM_SLACK = 32;

    private static final Deque<Entry> history = new ArrayDeque<>();

    private static List<Entry> snapshot;
    private static List<ChatTab> claimingTabs;
    private static List<ChatTab> allTabs = List.of();
    private static long visibilityStamp;
    private static boolean rebuilding;
    private static boolean inWorld;
    private static String lastServerAddress;

    private ChatTabs() {
    }

    public static boolean rebuilding() {
        return rebuilding;
    }

    public static void onMessage(Component message, MessageSignature signature, Object source,
                                 GuiMessageTag tag) {
        SchatConfig config = SchatConfig.get();
        refreshVisibility(config);
        Entry entry = new Entry(message, signature, source, tag, plainLowerCase(message));
        history.addLast(entry);
        trimHistory();

        boolean visible = false;
        for (ChatPanel panel : config.panels()) {
            if (panel.empty() || !visibleTo(entry, panel.activeTab())) {
                continue;
            }
            visible = true;
            show(panel, entry);
        }
        if (!visible) {
            countAsUnread(entry);
        }
    }

    private static void countAsUnread(Entry entry) {
        for (int index = 0; index < allTabs.size(); index++) {
            ChatTab tab = allTabs.get(index);
            if (visibleAt(entry, tab, index)) {
                tab.addUnread();
            }
        }
    }

    private static void show(ChatPanel panel, Entry entry) {
        ChatComponent component = panel.component();
        if (component == null) {
            return;
        }
        ChatTab tab = panel.activeTab();
        boolean repeat = tab.stackEnabled() && entry.normalized().equals(panel.lastShownText());
        if (repeat) {
            panel.setLastShown(entry.normalized(), panel.lastShownCount() + 1);
            removeLastShown(component, panel.lastLines());
        } else {
            panel.setLastShown(entry.normalized(), 1);
        }

        List<GuiMessage.Line> lines = ((ChatComponentAccessor) component).schat$trimmedMessages();
        int before = lines.size();
        rebuilding = true;
        try {
            append(component, entry, withCounter(
                    MessageButtons.decorate(entry.message(), entry.normalized(), tab),
                    panel.lastShownCount(), tab));
        } finally {
            rebuilding = false;
        }
        panel.setLastLines(Math.max(1, lines.size() - before));
    }

    private static void append(ChatComponent component, Entry entry, Component shown) {
        //? if >=26.1 {
        /*((ChatComponentAccessor) component).schat$addMessage(shown, entry.signature(),
                (net.minecraft.client.multiplayer.chat.GuiMessageSource) entry.source(),
                entry.tag());
        *///?} else {
        component.addMessage(shown, entry.signature(), entry.tag());
        //?}
    }

    private static void removeLastShown(ChatComponent component, int lines) {
        ChatComponentAccessor accessor = (ChatComponentAccessor) component;
        List<GuiMessage> messages = accessor.schat$allMessages();
        if (!messages.isEmpty()) {
            messages.remove(0);
        }
        List<GuiMessage.Line> trimmed = accessor.schat$trimmedMessages();
        for (int index = 0; index < lines && !trimmed.isEmpty(); index++) {
            trimmed.remove(0);
        }
    }

    private static Component withCounter(Component message, int count, ChatTab tab) {
        if (count <= 1) {
            return message;
        }
        ChatFormatting color = tab.stackColor();
        Component suffix = Component.literal(" " + formatCounter(tab, count))
                .withStyle(style -> style.withColor(color).withItalic(false));
        return Component.empty().append(message).append(suffix);
    }

    private static String formatCounter(ChatTab tab, int count) {
        try {
            return String.format(Locale.ROOT, tab.stackFormat(), count);
        } catch (RuntimeException ignored) {
            return String.format(Locale.ROOT, ChatTab.DEFAULT_STACK_FORMAT, count);
        }
    }

    public static void select(ChatPanel panel, int index) {
        panel.setActiveIndex(index);
        rebuild(panel);
        SchatConfig.get().save();
    }

    public static ChatTab addTab(ChatPanel panel) {
        ChatTab tab = ChatTab.named(Translations.get("schat.tab.new", panel.tabs().size() + 1));
        if (!panel.empty()) {
            tab.copyLookFrom(panel.tabs().get(0));
        }
        panel.addTab(tab);
        select(panel, panel.tabs().size() - 1);
        return tab;
    }

    public static boolean canRemoveTab(ChatPanel panel) {
        return panel.tabs().size() > 1 || !panel.primary();
    }

    public static void removeTab(ChatPanel panel, int index) {
        if (!canRemoveTab(panel)) {
            return;
        }
        SchatConfig config = SchatConfig.get();
        if (panel.tabs().size() == 1) {
            panel.takeTab(index);
            config.removePanel(panel);
        } else if (panel.removeTab(index)) {
            rebuild(panel);
        }
        config.save();
    }

    public static void rebuild(ChatPanel panel) {
        ChatComponent chat = panel.component();
        if (chat == null || panel.empty()) {
            return;
        }
        refreshVisibility(SchatConfig.get());
        ChatTab tab = panel.activeTab();
        int bit = allTabs.indexOf(tab);
        ChatComponentAccessor accessor = (ChatComponentAccessor) chat;
        accessor.schat$trimmedMessages().clear();
        accessor.schat$allMessages().clear();
        panel.setLastShown(null, 0);
        for (Entry entry : history) {
            if (visibleAt(entry, tab, bit)) {
                show(panel, entry);
            }
        }
        chat.resetChatScroll();
        tab.markRead();
    }

    // Ваниль зовёт getWidth/getScale из середины обхода своей же истории сообщений,
    // а мы на них висим миксином. Значит трогать вкладки и перестраивать чат можно
    // только из тика: иначе rebuild рвёт список под ногами у restoreState.
    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) {
            inWorld = false;
            return;
        }
        if (!SchatConfig.get().active()) {
            return;
        }
        String address = currentServerAddress();
        if (inWorld && Objects.equals(address, lastServerAddress)) {
            return;
        }
        inWorld = true;
        lastServerAddress = address;
        rebuildAll();
    }

    private static void ensureAvailableTabs() {
        for (ChatPanel panel : SchatConfig.get().panels()) {
            if (!panel.empty()) {
                panel.setShownIndex(availableIndex(panel));
            }
        }
    }

    // Выбранная игроком вкладка в приоритете: как только она снова доступна,
    // панель возвращается к ней сама.
    private static int availableIndex(ChatPanel panel) {
        List<ChatTab> tabs = panel.tabs();
        int chosen = panel.chosenIndex();
        if (chosen < tabs.size() && availableHere(tabs.get(chosen))) {
            return chosen;
        }
        for (int index = 0; index < tabs.size(); index++) {
            if (availableHere(tabs.get(index))) {
                return index;
            }
        }
        return chosen;
    }

    public static void rebuildAll() {
        invalidateClaims();
        ensureAvailableTabs();
        for (ChatPanel panel : SchatConfig.get().panels()) {
            rebuild(panel);
        }
    }

    public static boolean anyUnread(ChatPanel panel) {
        for (ChatTab tab : panel.tabs()) {
            if (tab.unread() > 0) {
                return true;
            }
        }
        return false;
    }

    public static void storeSnapshot() {
        snapshot = new ArrayList<>(history);
    }

    public static void restoreSnapshot() {
        if (snapshot == null) {
            return;
        }
        history.clear();
        history.addAll(snapshot);
        snapshot = null;
        rebuildAll();
    }

    public static void clearHistory() {
        if (transferringWorld()) {
            return;
        }
        history.clear();
        for (ChatPanel panel : SchatConfig.get().panels()) {
            for (ChatTab tab : panel.tabs()) {
                tab.markRead();
            }
            if (!panel.primary()) {
                clearMessages(panel.component());
            }
        }
    }

    // Смена мира идёт как storeState -> clearMessages -> restoreState, и чистка
    // посередине не должна уносить буфер вместе с собой.
    private static boolean transferringWorld() {
        return snapshot != null;
    }

    private static void clearMessages(ChatComponent component) {
        if (component == null) {
            return;
        }
        ChatComponentAccessor accessor = (ChatComponentAccessor) component;
        accessor.schat$trimmedMessages().clear();
        accessor.schat$allMessages().clear();
    }

    // Вкладка, привязанная к другому серверу, не показывается и не забирает сообщения:
    // на чужом сервере остаются только вкладки без привязки.
    public static boolean availableHere(ChatTab tab) {
        return tab.matchesServer(currentServerAddress());
    }

    private static String currentServerAddress() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getCurrentServer() == null) {
            return null;
        }
        return minecraft.getCurrentServer().ip;
    }

    private static boolean visibleIn(ChatTab tab, String text) {
        if (!availableHere(tab) || !tab.accepts(text)) {
            return false;
        }
        return tab.exclusive() || !claimedElsewhere(tab, text);
    }

    private static boolean claimedElsewhere(ChatTab tab, String text) {
        for (ChatTab other : claimingTabs()) {
            if (other != tab && availableHere(other) && other.accepts(text)) {
                return true;
            }
        }
        return false;
    }

    private static List<ChatTab> claimingTabs() {
        if (claimingTabs != null) {
            return claimingTabs;
        }
        claimingTabs = new ArrayList<>();
        for (ChatPanel panel : SchatConfig.get().panels()) {
            for (ChatTab tab : panel.tabs()) {
                if (tab.exclusive()) {
                    claimingTabs.add(tab);
                }
            }
        }
        return claimingTabs;
    }

    public static void invalidateClaims() {
        claimingTabs = null;
        visibilityStamp = 0;
    }

    private static String plainLowerCase(Component message) {
        String stripped = ChatFormatting.stripFormatting(message.getString());
        return stripped == null ? "" : stripped.toLowerCase(Locale.ROOT);
    }

    // Буфер один на все панели, поэтому резать его с головы по общему лимиту нельзя:
    // поток из глобалки вытеснял бы редкие сообщения чужих вкладок. При уборке каждая
    // вкладка набирает с конца свои historyLimit записей, а что не набрала ни одна —
    // выбрасывается. Отсюда и потолок буфера: сумма лимитов всех вкладок.
    private static void trimHistory() {
        int capacity = historyCapacity();
        if (history.size() <= capacity + Math.max(TRIM_SLACK, capacity / 8)) {
            return;
        }
        int[] taken = new int[allTabs.size()];
        List<Entry> kept = new ArrayList<>();
        Iterator<Entry> newestFirst = history.descendingIterator();
        while (newestFirst.hasNext()) {
            Entry entry = newestFirst.next();
            boolean wanted = false;
            for (int index = 0; index < allTabs.size(); index++) {
                ChatTab tab = allTabs.get(index);
                if (taken[index] >= tab.historyLimit() || !visibleAt(entry, tab, index)) {
                    continue;
                }
                taken[index]++;
                wanted = true;
            }
            if (wanted) {
                kept.add(entry);
            }
        }
        history.clear();
        for (int index = kept.size() - 1; index >= 0; index--) {
            history.addLast(kept.get(index));
        }
    }

    private static int historyCapacity() {
        int capacity = 0;
        for (ChatTab tab : allTabs) {
            capacity += tab.historyLimit();
        }
        return Math.max(ChatTab.MIN_HISTORY, capacity);
    }

    // Состав вкладок и их фильтры меняются прямо по ходу игры, а кэш видимости в записях
    // привязан к порядку вкладок в этом списке, поэтому обе части пересобираются вместе.
    private static void refreshVisibility(SchatConfig config) {
        long stamp = currentStamp(config);
        if (stamp == visibilityStamp) {
            return;
        }
        visibilityStamp = stamp;
        claimingTabs = null;
        List<ChatTab> tabs = new ArrayList<>();
        for (ChatPanel panel : config.panels()) {
            tabs.addAll(panel.tabs());
        }
        allTabs = tabs;
    }

    private static long currentStamp(SchatConfig config) {
        String address = currentServerAddress();
        long stamp = address == null ? 1 : address.hashCode();
        for (ChatPanel panel : config.panels()) {
            for (ChatTab tab : panel.tabs()) {
                stamp = stamp * 31 + System.identityHashCode(tab);
                stamp = stamp * 31 + tab.visibilityRevision();
            }
        }
        return stamp == 0 ? 1 : stamp;
    }

    private static boolean visibleTo(Entry entry, ChatTab tab) {
        return visibleAt(entry, tab, allTabs.indexOf(tab));
    }

    private static boolean visibleAt(Entry entry, ChatTab tab, int bit) {
        if (bit < 0 || bit >= Long.SIZE || visibilityStamp == 0) {
            return visibleIn(tab, entry.normalized());
        }
        return (visibilityMask(entry) & (1L << bit)) != 0;
    }

    private static long visibilityMask(Entry entry) {
        if (entry.cachedStamp == visibilityStamp) {
            return entry.cachedMask;
        }
        long mask = 0L;
        int count = Math.min(allTabs.size(), Long.SIZE);
        for (int index = 0; index < count; index++) {
            if (visibleIn(allTabs.get(index), entry.normalized())) {
                mask |= 1L << index;
            }
        }
        entry.cachedStamp = visibilityStamp;
        entry.cachedMask = mask;
        return mask;
    }

    public static ChatPanel panelOf(Object component) {
        SchatConfig config = SchatConfig.get();
        if (!config.active()) {
            return null;
        }
        PanelBound bound = (PanelBound) component;
        ChatPanel panel = bound.schat$panel();
        if (panel == null && isMainComponent(component)) {
            config.main().bind((ChatComponent) component);
            panel = config.main();
        }
        return panel;
    }

    public static boolean isMainComponent(Object component) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.gui != null && minecraft.gui.getChat() == component;
    }
}
