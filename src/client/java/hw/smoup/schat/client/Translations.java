package hw.smoup.schat.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Файлы лежат в ванильном формате, но читаем их сами: без fabric-api ресурсы мода
// не попадают в ResourceManager, поэтому ни Language, ни I18n их не увидят.
public final class Translations {

    private static final Gson GSON = new Gson();
    private static final String FALLBACK = "en_us";

    private static Map<String, String> entries = Map.of();
    private static String loadedCode;

    private Translations() {
    }

    public static String get(String key, Object... args) {
        refresh();
        String pattern = entries.get(key);
        if (pattern == null) {
            return key;
        }
        return args.length == 0 ? pattern : String.format(Locale.ROOT, pattern, args);
    }

    private static void refresh() {
        String code = currentCode();
        if (code.equals(loadedCode)) {
            return;
        }
        Map<String, String> merged = new HashMap<>(load(FALLBACK));
        if (!FALLBACK.equals(code)) {
            merged.putAll(load(code));
        }
        entries = merged;
        loadedCode = code;
    }

    private static Map<String, String> load(String code) {
        String path = "/assets/schat/lang/" + code + ".json";
        try (InputStream stream = Translations.class.getResourceAsStream(path)) {
            if (stream == null) {
                return Map.of();
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                Map<String, String> loaded = GSON.fromJson(reader, new TypeToken<Map<String, String>>() {
                }.getType());
                return loaded == null ? Map.of() : loaded;
            }
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private static String currentCode() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null || minecraft.options.languageCode == null) {
            return FALLBACK;
        }
        return minecraft.options.languageCode;
    }
}
