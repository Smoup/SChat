package hw.smoup.schat.client.chat;

import hw.smoup.schat.client.Translations;
import hw.smoup.schat.client.config.ChatPanel;
import hw.smoup.schat.client.config.ChatTab;
import hw.smoup.schat.client.config.MessageFilter;
import hw.smoup.schat.client.config.SchatConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class TabSettingsScreen extends Screen {

    private static final int PANEL_WIDTH = 320;
    private static final int ROW_HEIGHT = 22;
    private static final int FILTER_ROWS = 4;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final List<ChatFormatting> COLORS = Arrays.stream(ChatFormatting.values())
            .filter(ChatFormatting::isColor)
            .toList();

    private final ChatPanel panel;
    private final ChatTab tab;

    private int scroll;
    //? if <1.21.9 {
    /*Screen previous;
    *///?}

    private TabSettingsScreen(ChatPanel panel) {
        super(Component.literal(Translations.get("schat.settings.title")));
        this.panel = panel;
        this.tab = panel.activeTab();
    }

    public static void open(ChatPanel panel) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return;
        }
        TabSettingsScreen screen = new TabSettingsScreen(panel);
        //? if >=1.21.9 {
        minecraft.gui.getChat().preserveCurrentChatScreen();
        //?} else {
        /*screen.previous = minecraft.screen;
        *///?}
        minecraft.setScreen(screen);
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = Math.max(10, height / 2 - 150);
        addNameRow(left, top);
        addOpacityRow(left, top + ROW_HEIGHT);
        addHistoryRow(left, top + ROW_HEIGHT * 2);
        addDecoratorRows(left, top);
        addFilterSection(left, top + ROW_HEIGHT * 6 + 8);
    }

    private void addNameRow(int left, int y) {
        EditBox nameBox = new EditBox(font, left + 80, y, 150, 18,
                Component.literal(Translations.get("schat.settings.name")));
        nameBox.setMaxLength(32);
        nameBox.setValue(tab.name());
        nameBox.setResponder(tab::setName);
        nameBox.setTooltip(tip("schat.settings.name.tip"));
        addRenderableWidget(nameBox);
    }

    private void addOpacityRow(int left, int y) {
        double opacity = tab.backgroundOpacity() != null
                ? tab.backgroundOpacity()
                : vanillaOpacity();
        SettingsSlider slider = new SettingsSlider(left + 80, y, 150, 18, opacity,
                TabSettingsScreen::percent, tab::setBackgroundOpacity);
        slider.setTooltip(tip("schat.settings.opacity.tip"));
        addRenderableWidget(slider);
        addRenderableWidget(applyAllButton(left + 236, y, "schat.settings.all.opacity.tip",
                target -> target.setBackgroundOpacity(tab.backgroundOpacity())));
    }

    private void addHistoryRow(int left, int y) {
        SettingsSlider slider = new SettingsSlider(left + 80, y, 150, 18,
                historyToSlider(tab.historyLimit()),
                value -> String.valueOf(sliderToHistory(value)),
                value -> tab.setHistoryLimit(sliderToHistory(value)));
        slider.setTooltip(tip("schat.settings.history.tip"));
        addRenderableWidget(slider);
        addRenderableWidget(applyAllButton(left + 236, y, "schat.settings.all.history.tip",
                target -> target.setHistoryLimit(tab.historyLimit())));
    }

    private void addDecoratorRows(int left, int top) {
        decoratedRow(left, top + ROW_HEIGHT * 3, "stack", tab.stackEnabled(),
                tab.stackFormat(), tab.stackColor(), stackPreview(tab),
                tab::setStackEnabled, tab::setStackFormat, tab::setStackColor,
                target -> {
                    target.setStackEnabled(tab.stackEnabled());
                    target.setStackFormat(tab.stackFormat());
                    target.setStackColor(tab.stackColor());
                });

        decoratedRow(left, top + ROW_HEIGHT * 4, "nick", tab.nickButton(),
                tab.nickFormat(), tab.nickColor(), label(tab.nickFormat(), "Steve"),
                tab::setNickButton, tab::setNickFormat, tab::setNickColor,
                target -> {
                    target.setNickButton(tab.nickButton());
                    target.setNickFormat(tab.nickFormat());
                    target.setNickColor(tab.nickColor());
                });

        decoratedRow(left, top + ROW_HEIGHT * 5, "copy", tab.copyButton(),
                tab.copyFormat(), tab.copyColor(), tab.copyFormat(),
                tab::setCopyButton, tab::setCopyFormat, tab::setCopyColor,
                target -> {
                    target.setCopyButton(tab.copyButton());
                    target.setCopyFormat(tab.copyFormat());
                    target.setCopyColor(tab.copyColor());
                });
    }

    private void addFilterSection(int left, int filtersRow) {
        addRenderableWidget(Button.builder(
                        Component.literal(Translations.get(tab.exclusive()
                                ? "schat.settings.exclusive.on"
                                : "schat.settings.exclusive.off")),
                        press -> {
                            tab.setExclusive(!tab.exclusive());
                            rebuildWidgets();
                        })
                .bounds(left + 80, filtersRow, 76, 18)
                .tooltip(tip("schat.settings.exclusive.tip"))
                .build());

        Button addButton = Button.builder(
                        Component.literal(Translations.get("schat.settings.filter.add")),
                        press -> addFilter())
                .bounds(left + 160, filtersRow, 70, 18)
                .tooltip(tip("schat.settings.filter.add.tip"))
                .build();
        addRenderableWidget(addButton);
        addRenderableWidget(applyAllButton(left + 236, filtersRow, "schat.settings.all.filters.tip",
                this::copyFiltersTo));

        int firstFilter = filtersRow + ROW_HEIGHT;
        List<MessageFilter> filters = tab.filters();
        scroll = Math.min(scroll, Math.max(0, filters.size() - FILTER_ROWS));
        for (int row = 0; row < FILTER_ROWS; row++) {
            int index = scroll + row;
            if (index >= filters.size()) {
                break;
            }
            addFilterRow(filters.get(index), index, left, firstFilter + row * ROW_HEIGHT);
        }

        addRenderableWidget(Button.builder(
                        Component.literal(Translations.get("schat.settings.done")),
                        press -> onClose())
                .bounds(left + PANEL_WIDTH / 2 - 50, firstFilter + FILTER_ROWS * ROW_HEIGHT + 8, 100, 20)
                .tooltip(tip("schat.settings.done.tip"))
                .build());
    }

    private void decoratedRow(int left, int y, String key, boolean enabled, String format,
                              ChatFormatting color, String preview,
                              Consumer<Boolean> toggle, Consumer<String> setFormat,
                              Consumer<ChatFormatting> setColor, Consumer<ChatTab> applyAll) {
        addRenderableWidget(Button.builder(
                        Component.literal(Translations.get("schat.settings." + key
                                + (enabled ? ".on" : ".off"))),
                        press -> {
                            toggle.accept(!enabled);
                            rebuildWidgets();
                        })
                .bounds(left + 80, y, 50, 18)
                .tooltip(tip("schat.settings." + key + ".tip"))
                .build());

        EditBox formatBox = new EditBox(font, left + 134, y, 50, 18,
                Component.literal(Translations.get("schat.settings.format")));
        formatBox.setMaxLength(24);
        formatBox.setValue(format);
        formatBox.setResponder(setFormat::accept);
        formatBox.setTooltip(tip("schat.settings." + key + ".format.tip"));
        addRenderableWidget(formatBox);

        addRenderableWidget(Button.builder(
                        Component.literal(preview).withStyle(color),
                        press -> {
                            setColor.accept(nextColor(color));
                            rebuildWidgets();
                        })
                .bounds(left + 188, y, 42, 18)
                .tooltip(tip("schat.settings.color.tip"))
                .build());
        addRenderableWidget(applyAllButton(left + 236, y, "schat.settings.all." + key + ".tip",
                applyAll));
    }

    private void addFilterRow(MessageFilter filter, int index, int left, int y) {
        addRenderableWidget(Button.builder(
                        Component.literal(Translations.get(filter.labelKey())),
                        press -> {
                            filter.cycle();
                            rebuildWidgets();
                        })
                .bounds(left, y, 120, 18)
                .tooltip(tip("schat.settings.filter.mode.tip"))
                .build());

        EditBox box = new EditBox(font, left + 124, y, 158, 18,
                Component.literal(Translations.get("schat.settings.filter.text")));
        box.setMaxLength(64);
        box.setValue(filter.text());
        box.setResponder(filter::setText);
        box.setTooltip(tip("schat.settings.filter.text.tip"));
        addRenderableWidget(box);

        addRenderableWidget(Button.builder(Component.literal("x"), press -> {
                    tab.filters().remove(index);
                    rebuildWidgets();
                })
                .bounds(left + 286, y, 18, 18)
                .tooltip(tip("schat.settings.filter.remove.tip"))
                .build());
    }

    private Button applyAllButton(int x, int y, String tooltipKey, Consumer<ChatTab> action) {
        return Button.builder(Component.literal(Translations.get("schat.settings.all")), press -> {
                    for (ChatPanel other : SchatConfig.get().panels()) {
                        for (ChatTab target : other.tabs()) {
                            if (target != tab) {
                                action.accept(target);
                            }
                        }
                    }
                    ChatTabs.rebuildAll();
                    SchatConfig.get().save();
                })
                .bounds(x, y, 48, 18)
                .tooltip(tip(tooltipKey))
                .build();
    }

    private static Tooltip tip(String key) {
        return Tooltip.create(Component.literal(Translations.get(key)));
    }

    private void copyFiltersTo(ChatTab target) {
        List<MessageFilter> copies = new ArrayList<>();
        for (MessageFilter filter : tab.filters()) {
            copies.add(filter.copy());
        }
        target.setFilters(copies);
    }

    private void addFilter() {
        tab.filters().add(MessageFilter.empty());
        scroll = Math.max(0, tab.filters().size() - FILTER_ROWS);
        rebuildWidgets();
    }

    //? if >=1.20.2 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scrollFilters(scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    //?} else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return scrollFilters(amount) || super.mouseScrolled(mouseX, mouseY, amount);
    }
    *///?}

    private boolean scrollFilters(double amount) {
        int max = Math.max(0, tab.filters().size() - FILTER_ROWS);
        int next = Math.min(max, Math.max(0, scroll - (int) Math.signum(amount)));
        if (next == scroll) {
            return false;
        }
        scroll = next;
        rebuildWidgets();
        return true;
    }

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                                   int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        drawLabels(graphics::text);
    }
    *///?} else {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawLabels(graphics::drawString);
    }
    //?}

    private void drawLabels(ChatOverlay.TextSink sink) {
        int left = (width - PANEL_WIDTH) / 2;
        int top = Math.max(10, height / 2 - 150);
        sink.draw(font, title.getString(), left, top - 16, LABEL_COLOR);
        sink.draw(font, Translations.get("schat.settings.name"), left, top + 5, LABEL_COLOR);
        sink.draw(font, Translations.get("schat.settings.opacity"),
                left, top + ROW_HEIGHT + 5, LABEL_COLOR);
        sink.draw(font, Translations.get("schat.settings.history"),
                left, top + ROW_HEIGHT * 2 + 5, LABEL_COLOR);
        sink.draw(font, Translations.get("schat.settings.stack"),
                left, top + ROW_HEIGHT * 3 + 5, LABEL_COLOR);
        sink.draw(font, Translations.get("schat.settings.nick"),
                left, top + ROW_HEIGHT * 4 + 5, LABEL_COLOR);
        sink.draw(font, Translations.get("schat.settings.copy"),
                left, top + ROW_HEIGHT * 5 + 5, LABEL_COLOR);
        sink.draw(font, Translations.get("schat.settings.filters"),
                left, top + ROW_HEIGHT * 6 + 13, LABEL_COLOR);
    }

    @Override
    public void onClose() {
        ChatTabs.rebuildAll();
        SchatConfig.get().save();
        Minecraft minecraft = Minecraft.getInstance();
        //? if >=1.21.9 {
        minecraft.setScreen(minecraft.gui.getChat().restoreChatScreen());
        //?} else {
        /*minecraft.setScreen(previous);
        *///?}
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String stackPreview(ChatTab tab) {
        try {
            return String.format(Locale.ROOT, tab.stackFormat(), 3);
        } catch (RuntimeException ignored) {
            return String.format(Locale.ROOT, ChatTab.DEFAULT_STACK_FORMAT, 3);
        }
    }

    private static String label(String format, String value) {
        return format.contains("%s") ? format.replace("%s", value) : format;
    }

    private static ChatFormatting nextColor(ChatFormatting current) {
        return COLORS.get((COLORS.indexOf(current) + 1) % COLORS.size());
    }

    private static String percent(double value) {
        return String.format(Locale.ROOT, "%d%%", Math.round(value * 100));
    }

    private static double vanillaOpacity() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null || minecraft.options == null
                ? 0.5
                : minecraft.options.textBackgroundOpacity().get();
    }

    private static int sliderToHistory(double value) {
        return (int) Math.round(ChatTab.MIN_HISTORY
                + value * (ChatTab.MAX_HISTORY - ChatTab.MIN_HISTORY));
    }

    private static double historyToSlider(int history) {
        return (history - ChatTab.MIN_HISTORY)
                / (double) (ChatTab.MAX_HISTORY - ChatTab.MIN_HISTORY);
    }
}
