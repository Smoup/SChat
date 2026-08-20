package hw.smoup.schat.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hw.smoup.schat.client.Translations;
import hw.smoup.schat.client.chat.ChatTabs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.ChatComponent;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SchatConfig {

    public static final int BOTTOM_MARGIN = 40;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("schat.json");

    private static SchatConfig instance;

    private List<ChatPanel> panels = new ArrayList<>();

    private transient boolean fromDisk;
    private transient boolean initialized;
    private transient int lastSyncTick = -1;
    private transient double lastVanillaWidth;
    private transient double lastVanillaHeightFocused;
    private transient double lastVanillaHeightUnfocused;
    private transient double lastVanillaScale;

    public static SchatConfig get() {
        if (instance == null) {
            instance = read();
        }
        return instance;
    }

    public boolean active() {
        if (!ensureInitialized()) {
            return false;
        }
        syncFromVanilla();
        return true;
    }

    public ChatPanel main() {
        return panels.get(0);
    }

    public List<ChatPanel> panels() {
        return panels;
    }

    public ChatPanel addPanel(ChatPanel origin, int screenLeft, int screenBottom) {
        ChatPanel panel = origin.copyGeometry();
        panel.setPositionFromScreen(screenLeft, screenBottom);
        panels.add(panel);
        return panel;
    }

    public void removePanel(ChatPanel panel) {
        if (!panel.primary()) {
            panels.remove(panel);
        }
    }

    public void store() {
        pushToVanilla();
        save();
    }

    public void save() {
        ChatTabs.invalidateClaims();
        write();
    }

    private static SchatConfig read() {
        SchatConfig loaded = null;
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                loaded = GSON.fromJson(reader, SchatConfig.class);
            } catch (Exception ignored) {
            }
        }
        if (loaded == null) {
            return new SchatConfig().sanitize(false);
        }
        return loaded.sanitize(true);
    }

    private SchatConfig sanitize(boolean fromDisk) {
        this.fromDisk = fromDisk;
        if (panels == null) {
            panels = new ArrayList<>();
        }
        panels.removeIf(panel -> panel == null);
        if (panels.isEmpty()) {
            panels.add(new ChatPanel());
        }
        String defaultName = Translations.get("schat.tab.default");
        for (int index = 0; index < panels.size(); index++) {
            ChatPanel panel = panels.get(index);
            panel.setPrimary(index == 0);
            panel.sanitize(defaultName);
        }
        return this;
    }

    private void write() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }

    private boolean ensureInitialized() {
        if (initialized) {
            return true;
        }
        Options options = options();
        if (options == null) {
            return false;
        }
        if (!fromDisk) {
            ChatPanel panel = main();
            panel.setWidth(ChatComponent.getWidth(options.chatWidth().get()));
            panel.setHeight(true, ChatComponent.getHeight(options.chatHeightFocused().get()));
            panel.setHeight(false, ChatComponent.getHeight(options.chatHeightUnfocused().get()));
            panel.setScale(options.chatScale().get());
        }
        rememberVanilla(options);
        initialized = true;
        return true;
    }

    private void syncFromVanilla() {
        Options options = options();
        if (options == null || alreadySyncedThisTick()) {
            return;
        }
        ChatPanel panel = main();
        double vanillaWidth = options.chatWidth().get();
        if (vanillaWidth != lastVanillaWidth) {
            panel.setWidth(ChatComponent.getWidth(vanillaWidth));
            lastVanillaWidth = vanillaWidth;
        }
        double vanillaHeightFocused = options.chatHeightFocused().get();
        if (vanillaHeightFocused != lastVanillaHeightFocused) {
            panel.setHeight(true, ChatComponent.getHeight(vanillaHeightFocused));
            lastVanillaHeightFocused = vanillaHeightFocused;
        }
        double vanillaHeightUnfocused = options.chatHeightUnfocused().get();
        if (vanillaHeightUnfocused != lastVanillaHeightUnfocused) {
            panel.setHeight(false, ChatComponent.getHeight(vanillaHeightUnfocused));
            lastVanillaHeightUnfocused = vanillaHeightUnfocused;
        }
        double vanillaScale = options.chatScale().get();
        if (vanillaScale != lastVanillaScale) {
            panel.setScale(vanillaScale);
            lastVanillaScale = vanillaScale;
        }
        ChatTabs.ensureAvailableTabs();
    }

    private void pushToVanilla() {
        Options options = options();
        if (options == null) {
            return;
        }
        ChatPanel panel = main();
        options.chatWidth().set(toVanillaWidth(panel.width()));
        options.chatHeightFocused().set(toVanillaHeight(panel.height(true)));
        options.chatHeightUnfocused().set(toVanillaHeight(panel.height(false)));
        options.chatScale().set(Math.min(1.0, panel.scale()));
        rememberVanilla(options);
        options.save();
    }

    private void rememberVanilla(Options options) {
        lastVanillaWidth = options.chatWidth().get();
        lastVanillaHeightFocused = options.chatHeightFocused().get();
        lastVanillaHeightUnfocused = options.chatHeightUnfocused().get();
        lastVanillaScale = options.chatScale().get();
    }

    private boolean alreadySyncedThisTick() {
        Minecraft minecraft = Minecraft.getInstance();
        int ticks = minecraft == null || minecraft.gui == null ? 0 : minecraft.gui.getGuiTicks();
        if (ticks == lastSyncTick) {
            return true;
        }
        lastSyncTick = ticks;
        return false;
    }

    private static Options options() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.options;
    }

    private static double toVanillaWidth(int pixels) {
        return clamp01((pixels - 40.0) / 280.0);
    }

    private static double toVanillaHeight(int pixels) {
        return clamp01((pixels - 20.0) / 160.0);
    }

    private static double clamp01(double value) {
        return Math.min(1.0, Math.max(0.0, value));
    }
}
