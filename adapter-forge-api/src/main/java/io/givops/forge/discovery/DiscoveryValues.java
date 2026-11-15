package io.givops.forge.discovery;

import io.givops.adapters.i18n.SdkMessages;
import io.givops.forge.model.ForgeException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.http.HttpStatus;

public final class DiscoveryValues {
    private DiscoveryValues() {}

    public static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(SdkMessages.format("forge.discoveryValues.sha256.unavailable"), exception);
        }
    }

    public static String suggestedKey(String path, String id) {
        String key = Normalizer.normalize(path, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (key.isEmpty() || key.charAt(0) < 'a' || key.charAt(0) > 'z') key = "project-" + key;
        String suffix = "-" + id;
        return key.substring(0, Math.min(key.length(), 80 - suffix.length())).replaceAll("-+$", "") + suffix;
    }

    public static long numericId(String value) {
        try {
            if (value == null || !value.matches("[1-9][0-9]{0,18}")) throw new NumberFormatException();
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw error("invalid_discovery_id", HttpStatus.BAD_REQUEST, SdkMessages.format("forge.discoveryValues.a.canonical.positive.numeric.forge.id.is.required"));
        }
    }

    public static ForgeException error(String code, HttpStatus status, String detail) {
        return new ForgeException(code, status, detail);
    }
}
