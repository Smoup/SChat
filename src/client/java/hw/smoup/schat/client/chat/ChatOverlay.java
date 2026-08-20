package hw.smoup.schat.client.chat;

import hw.smoup.schat.client.Translations;
import hw.smoup.schat.client.config.ChatPanel;
import hw.smoup.schat.client.config.ChatTab;
import hw.smoup.schat.client.config.SchatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class ChatOverlay {

    @FunctionalInterface
    public interface RectSink {
        void fill(int x1, int y1, int x2, int y2, int argb);
    }

    @FunctionalInterface
    public interface TextSink {
        void draw(Font font, String text, int x, int y, int argb);
    }

    public record Target(ChatPanel panel, boolean focused, Handle handle) {
    }

    private static final int GRAB = 3;
    private static final int HANDLE_THICKNESS = 2;
    private static final int CORNER_SIZE = 5;
    private static final int SCALE_SIZE = 7;
    private static final double SCALE_PER_PIXEL = 0.01;

    private static final int LINE_FOCUSED = 0x55FFFFFF;
    private static final int LINE_UNFOCUSED = 0x8866CCFF;
    private static final int HANDLE_IDLE = 0xAAFFFFFF;
    private static final int HANDLE_HOVER = 0xFFFFFFFF;
    private static final int HANDLE_DRAG = 0xFFFFC94D;
    private static final int SCALE_IDLE = 0xCC8CD97F;
    private static final int SCALE_HOVER = 0xFFB6F5A8;
    private static final int MOVE_IDLE = 0xCC8FB8E3;
    private static final int MOVE_HOVER = 0xFFAFD4FF;
    private static final int MERGE_HIGHLIGHT = 0x66FFC94D;
    private static final int LABEL_COLOR = 0xFFFFFFFF;

    private static Target dragging;
    private static boolean leftDown;
    private static boolean wasPressed;
    private static boolean resetRequested;
    private static boolean resetStoresDefault;
    private static int dragStartX;
    private static int dragStartY;
    private static double dragStartScale;
    private static int dragStartOffsetX;
    private static int dragStartOffsetY;

    private ChatOverlay() {
    }

    public static void onMouseButton(int button, int action, int modifiers) {
        if (button == 0) {
            leftDown = action != 0;
        } else if (button == 2 && action != 0 && chatScreenOpen()) {
            resetRequested = true;
            resetStoresDefault = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        }
    }

    public static void render(RectSink rects, TextSink texts, int mouseX, int mouseY) {
        if (!SchatConfig.get().active()) {
            return;
        }
        Target hovered = hitTest(mouseX, mouseY);

        if (resetRequested) {
            resetRequested = false;
            if (resetStoresDefault) {
                rememberDefault(hovered);
            } else {
                applyDefault(hovered);
            }
        }

        if (leftDown && !wasPressed && hovered != null) {
            beginDrag(hovered, mouseX, mouseY);
        } else if (!leftDown && dragging != null) {
            finishDrag(mouseX, mouseY);
        }
        wasPressed = leftDown;

        if (dragging != null) {
            applyDrag(mouseX, mouseY);
        }

        Target active = dragging != null ? dragging : hovered;
        ChatPanel mergeTarget = mergeTarget(mouseX, mouseY);
        for (ChatPanel panel : SchatConfig.get().panels()) {
            if (panel.empty()) {
                continue;
            }
            if (panel == mergeTarget) {
                highlight(rects, panel);
            }
            drawUnfocusedEdge(rects, panel, active);
            drawFrame(rects, panel, active);
        }
        if (active != null) {
            drawLabel(texts, active, mouseX, mouseY);
        }
    }

    public static boolean grabsClick(double mouseX, double mouseY) {
        return hitTest(mouseX, mouseY) != null;
    }

    private static void applyDefault(Target hovered) {
        if (hovered == null) {
            return;
        }
        ChatPanel panel = hovered.panel();
        Handle handle = hovered.handle();
        switch (handle) {
            case SCALE -> panel.setScale(panel.defaultScale());
            case MOVE -> panel.setOffset(panel.defaultOffsetX(), panel.defaultOffsetY());
            default -> {
                if (handle.affectsWidth()) {
                    panel.setWidth(panel.defaultWidth());
                }
                if (handle.affectsHeight()) {
                    panel.setHeight(hovered.focused(), panel.defaultHeight(hovered.focused()));
                }
            }
        }
        SchatConfig.get().store();
        rescale(panel);
    }

    private static void rememberDefault(Target hovered) {
        if (hovered == null) {
            return;
        }
        ChatPanel panel = hovered.panel();
        Handle handle = hovered.handle();
        switch (handle) {
            case SCALE -> panel.rememberScale();
            case MOVE -> panel.rememberOffset();
            default -> {
                if (handle.affectsWidth()) {
                    panel.rememberWidth();
                }
                if (handle.affectsHeight()) {
                    panel.rememberHeight(hovered.focused());
                }
            }
        }
        SchatConfig.get().save();
    }

    private static void beginDrag(Target target, int mouseX, int mouseY) {
        ChatPanel panel = target.panel();
        dragging = target;
        dragStartX = mouseX;
        dragStartY = mouseY;
        dragStartScale = panel.scale();
        dragStartOffsetX = panel.effectiveOffsetX();
        dragStartOffsetY = panel.effectiveOffsetY();
    }

    private static void finishDrag(int mouseX, int mouseY) {
        ChatPanel merge = mergeTarget(mouseX, mouseY);
        if (merge != null) {
            mergeInto(merge, dragging.panel());
        }
        SchatConfig.get().store();
        rescale(dragging.panel());
        dragging = null;
    }

    private static ChatPanel mergeTarget(int mouseX, int mouseY) {
        if (dragging == null || dragging.handle() != Handle.MOVE) {
            return null;
        }
        for (ChatPanel panel : SchatConfig.get().panels()) {
            if (panel == dragging.panel() || panel.empty()) {
                continue;
            }
            if (ChatFrame.of(panel, true).contains(mouseX, mouseY, 0)) {
                return panel;
            }
        }
        return null;
    }

    private static void mergeInto(ChatPanel target, ChatPanel source) {
        for (ChatTab tab : source.tabs()) {
            target.addTab(tab);
        }
        source.tabs().clear();
        if (source.primary()) {
            source.tabs().addAll(target.tabs());
            target.tabs().clear();
            SchatConfig.get().removePanel(target);
            source.setActiveIndex(source.activeIndex());
            ChatTabs.rebuild(source);
            return;
        }
        SchatConfig.get().removePanel(source);
        target.setActiveIndex(target.activeIndex());
        ChatTabs.rebuild(target);
    }

    private static void applyDrag(int mouseX, int mouseY) {
        ChatPanel panel = dragging.panel();
        int oldWidth = panel.width();
        double oldScale = panel.scale();

        switch (dragging.handle()) {
            case SCALE -> {
                double scale = dragStartScale + (dragStartY - mouseY) * SCALE_PER_PIXEL;
                panel.setScale(Math.round(scale * 100.0) / 100.0);
            }
            case MOVE -> panel.setOffset(
                    dragStartOffsetX + (mouseX - dragStartX),
                    dragStartOffsetY + (mouseY - dragStartY));
            default -> {
                if (dragging.handle().affectsWidth()) {
                    panel.setWidth(ChatFrame.widthFromMouse(panel, mouseX));
                }
                if (dragging.handle().affectsHeight()) {
                    panel.setHeight(dragging.focused(), ChatFrame.heightFromMouse(panel, mouseY));
                }
            }
        }

        if (panel.width() != oldWidth || panel.scale() != oldScale) {
            rescale(panel);
        }
    }

    private static Target hitTest(double mouseX, double mouseY) {
        for (ChatPanel panel : SchatConfig.get().panels()) {
            if (panel.empty()) {
                continue;
            }
            Target target = hitTest(panel, mouseX, mouseY);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    private static Target hitTest(ChatPanel panel, double mouseX, double mouseY) {
        ChatFrame frame = ChatFrame.of(panel, true);
        boolean inColumn = mouseX >= frame.left() - GRAB && mouseX <= frame.right() + GRAB;
        boolean onRight = Math.abs(mouseX - frame.right()) <= GRAB
                && mouseY >= frame.top() - GRAB && mouseY <= frame.bottom() + GRAB;
        boolean onTop = Math.abs(mouseY - frame.top()) <= GRAB && inColumn;
        boolean onBottom = Math.abs(mouseY - frame.bottom()) <= GRAB && inColumn;
        boolean onScale = mouseX >= frame.left() - GRAB && mouseX <= frame.left() + SCALE_SIZE
                && mouseY >= frame.top() - GRAB && mouseY <= frame.top() + SCALE_SIZE;

        if (onRight && onTop) {
            return new Target(panel, true, Handle.CORNER);
        }
        if (onScale) {
            return new Target(panel, true, Handle.SCALE);
        }
        if (onBottom) {
            return new Target(panel, true, Handle.MOVE);
        }
        if (onRight) {
            return new Target(panel, true, Handle.WIDTH);
        }
        if (onTop) {
            return new Target(panel, true, Handle.HEIGHT);
        }
        ChatFrame unfocused = ChatFrame.of(panel, false);
        if (Math.abs(mouseY - unfocused.top()) <= GRAB
                && mouseX >= unfocused.left() - GRAB && mouseX <= unfocused.right() + GRAB) {
            return new Target(panel, false, Handle.HEIGHT);
        }
        return null;
    }

    private static void highlight(RectSink sink, ChatPanel panel) {
        ChatFrame frame = ChatFrame.of(panel, true);
        sink.fill(frame.left(), frame.top(), frame.right(), frame.bottom(), MERGE_HIGHLIGHT);
    }

    private static void drawFrame(RectSink sink, ChatPanel panel, Target active) {
        ChatFrame frame = ChatFrame.of(panel, true);
        sink.fill(frame.left(), frame.top(), frame.right(), frame.top() + 1, LINE_FOCUSED);
        sink.fill(frame.right(), frame.top(), frame.right() + 1, frame.bottom() + 1, LINE_FOCUSED);
        sink.fill(frame.left(), frame.bottom(), frame.right(), frame.bottom() + 1, LINE_FOCUSED);
        sink.fill(frame.left(), frame.top(), frame.left() + 1, frame.bottom(), LINE_FOCUSED);

        Handle handle = active != null && active.panel() == panel && active.focused()
                ? active.handle()
                : null;
        int color = dragging != null && handle != null ? HANDLE_DRAG : HANDLE_HOVER;

        if (handle != null && handle.affectsWidth()) {
            sink.fill(frame.right() - HANDLE_THICKNESS + 1, frame.top(),
                    frame.right() + HANDLE_THICKNESS, frame.bottom(), color);
        }
        if (handle != null && handle.affectsHeight()) {
            sink.fill(frame.left(), frame.top() - HANDLE_THICKNESS + 1,
                    frame.right(), frame.top() + HANDLE_THICKNESS, color);
        }
        if (handle == Handle.CORNER) {
            sink.fill(frame.right() - CORNER_SIZE, frame.top() - CORNER_SIZE,
                    frame.right() + 1, frame.top() + 1, color);
        } else if (handle == null) {
            sink.fill(frame.right() - CORNER_SIZE + 2, frame.top() - 1,
                    frame.right() + 1, frame.top() + 1, HANDLE_IDLE);
        }

        drawScaleHandle(sink, frame, handle == Handle.SCALE);
        drawMoveHandle(sink, frame, handle == Handle.MOVE);
    }

    private static void drawUnfocusedEdge(RectSink sink, ChatPanel panel, Target active) {
        ChatFrame frame = ChatFrame.of(panel, false);
        boolean isActive = active != null && active.panel() == panel && !active.focused();
        if (isActive) {
            int color = dragging != null ? HANDLE_DRAG : HANDLE_HOVER;
            sink.fill(frame.left(), frame.top() - HANDLE_THICKNESS + 1,
                    frame.right(), frame.top() + HANDLE_THICKNESS, color);
            return;
        }
        dashedHorizontal(sink, frame.left(), frame.right(), frame.top(), LINE_UNFOCUSED);
    }

    private static void drawScaleHandle(RectSink sink, ChatFrame frame, boolean active) {
        int color = active ? (dragging != null ? HANDLE_DRAG : SCALE_HOVER) : SCALE_IDLE;
        int size = active ? SCALE_SIZE + 2 : SCALE_SIZE;
        int thickness = active ? 3 : 2;
        sink.fill(frame.left(), frame.top(), frame.left() + size, frame.top() + thickness, color);
        sink.fill(frame.left(), frame.top(), frame.left() + thickness, frame.top() + size, color);
    }

    private static void drawMoveHandle(RectSink sink, ChatFrame frame, boolean active) {
        int color = active ? (dragging != null ? HANDLE_DRAG : MOVE_HOVER) : MOVE_IDLE;
        if (active) {
            sink.fill(frame.left(), frame.bottom() - HANDLE_THICKNESS + 1,
                    frame.right(), frame.bottom() + HANDLE_THICKNESS, color);
            return;
        }
        int middle = (frame.left() + frame.right()) / 2;
        sink.fill(middle - 8, frame.bottom() - 1, middle + 8, frame.bottom() + 1, color);
    }

    private static void drawLabel(TextSink sink, Target active, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.font == null) {
            return;
        }
        ChatPanel panel = active.panel();
        String value = switch (active.handle()) {
            case SCALE -> Translations.get("schat.handle.scale",
                    String.format(Locale.ROOT, "%.2f", panel.scale()));
            case MOVE -> Translations.get("schat.handle.position",
                    panel.effectiveOffsetX(), panel.effectiveOffsetY());
            case WIDTH -> Translations.get("schat.handle.width", panel.effectiveWidth());
            default -> Translations.get("schat.handle.size",
                    panel.effectiveWidth(), panel.effectiveHeight(active.focused()));
        };
        String label = dragging != null
                ? Translations.get("schat.label", scope(active), value)
                : Translations.get("schat.label.hint", scope(active), value,
                        Translations.get("schat.hint.reset"));
        int width = minecraft.font.width(label);
        int x = Math.max(2, Math.min(mouseX + 8, ChatFrame.guiWidth() - width - 2));
        int y = Math.max(2, mouseY - 12);
        sink.draw(minecraft.font, label, x, y, LABEL_COLOR);
    }

    private static String scope(Target active) {
        if (active.handle().affectsHeight()) {
            return Translations.get(active.focused() ? "schat.chat.open" : "schat.chat.closed");
        }
        return Translations.get("schat.chat.both");
    }

    private static void dashedHorizontal(RectSink sink, int from, int to, int y, int color) {
        for (int x = from; x < to; x += 4) {
            sink.fill(x, y, Math.min(x + 2, to), y + 1, color);
        }
    }

    private static boolean chatScreenOpen() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.screen instanceof ChatScreen;
    }

    private static void rescale(ChatPanel panel) {
        if (panel.component() != null) {
            panel.component().rescaleChat();
        }
    }
}
