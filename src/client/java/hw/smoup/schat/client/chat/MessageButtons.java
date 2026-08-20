package hw.smoup.schat.client.chat;

import hw.smoup.schat.client.Compat;
import hw.smoup.schat.client.Translations;
import hw.smoup.schat.client.config.ChatTab;
import hw.smoup.schat.client.config.MessageFilter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class MessageButtons {

    private static final String ADMINTOOL_MARK = "⧉";
    private static final int MIN_NICK = 3;

    private static final List<String> nicks = new ArrayList<>();
    private static final List<String> loweredNicks = new ArrayList<>();
    private static int nicksTick = -1;

    private MessageButtons() {
    }

    public static Component decorate(Component message, String normalized, ChatTab tab) {
        Component result = stripFilterParts(message, tab);
        if (!tab.nickButton() && !tab.copyButton()) {
            return result;
        }
        message = result;

        if (tab.nickButton() && !normalized.contains(ADMINTOOL_MARK)) {
            String nick = findNick(normalized);
            if (nick != null) {
                result = insertAfterNick(result, nick, button(
                        label(tab.nickFormat(), nick),
                        tab.nickColor(),
                        nick,
                        Translations.get("schat.nick.copy", nick)));
            }
        }

        if (tab.copyButton()) {
            String text = message.getString();
            result = Component.empty().append(result).append(button(
                    label(tab.copyFormat(), text),
                    tab.copyColor(),
                    text,
                    Translations.get("schat.message.copy")));
        }

        return result;
    }

    // Фильтр с пометкой «вырезать» убирает из показанного текста ту часть, по которой
    // сообщение сюда и попало: раз вкладка про неё, повторять её в каждой строке незачем.
    private static Component stripFilterParts(Component message, ChatTab tab) {
        List<String> parts = new ArrayList<>();
        for (MessageFilter filter : tab.filters()) {
            if (filter.strip() && !filter.blank() && !filter.negate()) {
                parts.add(filter.lowered());
            }
        }
        if (parts.isEmpty()) {
            return message;
        }
        boolean[] cut = markCutPositions(message.getString(), parts);
        if (cut == null) {
            return message;
        }
        MutableComponent result = Component.empty();
        int[] offset = {0};
        message.visit((style, part) -> {
            String kept = keepUncut(part, cut, offset[0]);
            offset[0] += part.length();
            if (!kept.isEmpty()) {
                result.append(Component.literal(kept).withStyle(style));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    // Искать надо по всему сообщению: сервер часто разрывает строку на куски со своими
    // стилями, и подстрока фильтра может лежать сразу в двух из них.
    private static boolean[] markCutPositions(String text, List<String> parts) {
        boolean[] cut = new boolean[text.length()];
        String haystack = text.toLowerCase(Locale.ROOT);
        boolean found = false;
        for (String part : parts) {
            int index = haystack.indexOf(part);
            while (index >= 0) {
                for (int position = index; position < index + part.length(); position++) {
                    cut[position] = true;
                }
                found = true;
                index = haystack.indexOf(part, index + part.length());
            }
        }
        return found ? cut : null;
    }

    private static String keepUncut(String part, boolean[] cut, int offset) {
        StringBuilder kept = new StringBuilder(part.length());
        for (int index = 0; index < part.length(); index++) {
            int position = offset + index;
            if (position >= cut.length || !cut[position]) {
                kept.append(part.charAt(index));
            }
        }
        return kept.toString();
    }

    private static Component insertAfterNick(Component message, String nick, Component button) {
        MutableComponent result = Component.empty();
        boolean[] inserted = {false};
        String needle = nick.toLowerCase(Locale.ROOT);
        message.visit((style, part) -> {
            int index = inserted[0] ? -1 : indexOfWord(part.toLowerCase(Locale.ROOT), needle);
            if (index < 0) {
                result.append(Component.literal(part).withStyle(style));
                return Optional.empty();
            }
            int end = index + nick.length();
            result.append(Component.literal(part.substring(0, end)).withStyle(style));
            result.append(button);
            if (end < part.length()) {
                result.append(Component.literal(part.substring(end)).withStyle(style));
            }
            inserted[0] = true;
            return Optional.empty();
        }, Style.EMPTY);
        return inserted[0] ? result : message;
    }

    private static String label(String format, String value) {
        return format.contains("%s") ? format.replace("%s", value) : format;
    }

    private static Component button(String label, ChatFormatting color, String clipboard,
                                    String hover) {
        return Component.literal(" " + label).withStyle(style -> style
                .withColor(color)
                .withItalic(false)
                .withClickEvent(Compat.copyToClipboard(clipboard))
                .withHoverEvent(Compat.showText(Component.literal(hover))));
    }

    private static String findNick(String normalized) {
        refreshNicks();
        for (int index = 0; index < loweredNicks.size(); index++) {
            if (indexOfWord(normalized, loweredNicks.get(index)) >= 0) {
                return nicks.get(index);
            }
        }
        return null;
    }

    private static void refreshNicks() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return;
        }
        int ticks = minecraft.gui.getGuiTicks();
        if (ticks == nicksTick) {
            return;
        }
        nicksTick = ticks;
        nicks.clear();
        loweredNicks.clear();
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) {
            return;
        }
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            String name = Compat.profileName(info.getProfile());
            if (name == null || name.length() < MIN_NICK) {
                continue;
            }
            nicks.add(name);
            loweredNicks.add(name.toLowerCase(Locale.ROOT));
        }
    }

    private static int indexOfWord(String haystack, String needle) {
        int from = haystack.indexOf(needle);
        while (from >= 0) {
            int end = from + needle.length();
            boolean leftFree = from == 0 || !isNickChar(haystack.charAt(from - 1));
            boolean rightFree = end == haystack.length() || !isNickChar(haystack.charAt(end));
            if (leftFree && rightFree) {
                return from;
            }
            from = haystack.indexOf(needle, from + 1);
        }
        return -1;
    }

    private static boolean isNickChar(char symbol) {
        return Character.isLetterOrDigit(symbol) || symbol == '_';
    }
}
