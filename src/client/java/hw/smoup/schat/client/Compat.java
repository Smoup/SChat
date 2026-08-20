package hw.smoup.schat.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public final class Compat {

    private Compat() {
    }

    public static ClickEvent copyToClipboard(String value) {
        //? if >=1.21.5 {
        return new ClickEvent.CopyToClipboard(value);
        //?} else {
        /*return new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value);
        *///?}
    }

    public static HoverEvent showText(Component text) {
        //? if >=1.21.5 {
        return new HoverEvent.ShowText(text);
        //?} else {
        /*return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        *///?}
    }

    public static boolean shiftDown() {
        //? if >=1.21.9 {
        return net.minecraft.client.Minecraft.getInstance().hasShiftDown();
        //?} else {
        /*return net.minecraft.client.gui.screens.Screen.hasShiftDown();
        *///?}
    }

    public static String profileName(GameProfile profile) {
        //? if >=1.21.9 {
        return profile.name();
        //?} else {
        /*return profile.getName();
        *///?}
    }
}
