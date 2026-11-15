package io.givops.adapters;

import static org.assertj.core.api.Assertions.*;

import io.givops.adapters.i18n.MessageCatalog;
import io.givops.adapters.i18n.SdkMessages;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class MessageCatalogTest {
    private MessageCatalog catalog(String yaml) {
        return MessageCatalog.load(Map.of("test.yml", new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))));
    }
    @Test void strictPatternsPreserveNamedValuesAndQuoting() {
        var catalog = catalog("message: \"The version ''{name}'' has {count, plural, one{# item} other{# items}}.\"");
        assertThat(catalog.format("message", Map.of("name", "Ada's {release}", "count", 2)))
                .isEqualTo("The version 'Ada's {release}' has 2 items.");
        assertThatThrownBy(() -> catalog.format("message", Map.of("count", 2))).hasMessageContaining("Missing message arguments");
        assertThatThrownBy(() -> catalog.format("absent", Map.of())).hasMessageContaining("Unknown message key");
    }
    @Test void rejectsUnsafeYamlAndInvalidMf1() {
        for (String yaml : new String[]{"a: one\na: two", "a: 3", "a: null", "a: [text]", "a: &copy text\nb: *copy",
                "a: !!java.lang.String text", "a: '{0}'", "a: '{broken'", "a: '{n, plural, one{one}}'"})
            assertThatThrownBy(() -> catalog(yaml)).as(yaml).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> MessageCatalog.load(getClass(), "/missing.yml"))
                .hasMessageContaining("Missing message catalog");
    }
    @Test void formattingIsConcurrentAndEnglishFallbackIsExplicit() throws Exception {
        var catalog = catalog("count: '{count, plural, one{# item} other{# items}}'");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = java.util.stream.IntStream.range(2, 100).mapToObj(count -> executor.submit(() ->
                    assertThat(catalog.format("count", Map.of("count", count))).isEqualTo(count + " items"))).toList();
            for (var future : futures) future.get();
        }
        assertThat(SdkMessages.format(Locale.JAPANESE, "sdk.credential.unavailable", Map.of()))
                .isEqualTo("Credential access is unavailable.");
    }
}
