package hw.smoup.schat.client.chat;

import hw.smoup.schat.client.config.ChatPanel;
import hw.smoup.schat.client.config.SchatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Style;
//? if >=1.21.11 {
import net.minecraft.client.gui.ActiveTextCollector;
//?}

public final class ChatLinks {

    private ChatLinks() {
    }

    public static Style styleAt(Font font, double mouseX, double mouseY, boolean insertions) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return null;
        }
        for (ChatPanel panel : SchatConfig.get().panels()) {
            ChatComponent component = panel.empty() ? null : panel.component();
            if (component == null) {
                continue;
            }
            Style style = styleIn(component, font, mouseX - panel.effectiveOffsetX(),
                    mouseY - panel.effectiveOffsetY() - ChatFrame.stateOffsetY(panel, true),
                    insertions);
            if (style != null) {
                return style;
            }
        }
        return null;
    }

    private static Style styleIn(ChatComponent component, Font font, double mouseX, double mouseY,
                                 boolean insertions) {
        //? if >=1.21.11 {
        Minecraft minecraft = Minecraft.getInstance();
        ActiveTextCollector.ClickableStyleFinder finder =
                new ActiveTextCollector.ClickableStyleFinder(font, (int) mouseX, (int) mouseY)
                        .includeInsertions(insertions);
        /*? if >=26.1 {*/
        /*component.captureClickableText(finder, ChatFrame.guiHeight(), minecraft.gui.getGuiTicks(),
                net.minecraft.client.gui.components.ChatComponent.DisplayMode.FOREGROUND);
        *//*?} else {*/
        component.captureClickableText(finder, ChatFrame.guiHeight(),
                minecraft.gui.getGuiTicks(), true);
        /*?}*/
        return finder.result();
        //?} else {
        /*return component.getClickedComponentStyleAt(mouseX, mouseY);
        *///?}
    }
}
