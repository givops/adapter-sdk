package io.givops.adapters.i18n;

import java.util.Locale;
import java.util.Map;

/** SDK-owned messages. English is the current supported language and fallback. */
public final class SdkMessages {
    private static final MessageCatalog ENGLISH = MessageCatalog.load(SdkMessages.class,
            "/io/givops/adapters/sdk/i18n/en.yml");
    private SdkMessages() { }
    public static String format(String key) { return format(key, Map.of()); }
    public static String format(String key, Map<String, ?> parameters) { return ENGLISH.format(key, parameters); }
    public static String format(Locale locale, String key, Map<String, ?> parameters) { return format(key, parameters); }
}
