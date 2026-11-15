package example.ticketing;

import io.givops.ticketing.model.TicketException;
import io.givops.ticketing.model.TicketException.Code;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import tools.jackson.databind.json.JsonMapper;

/** Opt-in local synthetic response fixture, never a live vendor API. */
final class ExamplePayload {
    private static final int MAX_BYTES = 4096;
    private final String nameField;
    ExamplePayload(String nameField) { this.nameField = nameField; }
    static ExamplePayload packaged() {
        var properties = new Properties();
        try (var input = ExamplePayload.class.getResourceAsStream("/example/ticketing/payload-schema.properties")) {
            if (input == null) throw new IllegalStateException("Missing example payload schema");
            properties.load(input);
            return new ExamplePayload(properties.getProperty("nameField"));
        } catch (IOException failure) { throw new IllegalStateException("Unreadable example payload schema"); }
    }
    String optionalName(String fallback) {
        String fixture = System.getProperty("givops.example.payload");
        return fixture == null ? fallback : name(Path.of(fixture));
    }
    String name(Path fixture) {
        try (var input = Files.newInputStream(fixture)) {
            byte[] bytes = input.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) throw invalid();
            var document = JsonMapper.builder().build().readTree(bytes);
            var value = document == null ? null : document.get(nameField);
            if (value == null || !value.isString() || value.asString().isBlank() || value.asString().length() > 255)
                throw invalid();
            return value.asString();
        } catch (IOException | tools.jackson.core.JacksonException failure) { throw invalid(); }
    }
    private static TicketException invalid() {
        return new TicketException(Code.INVALID_RESPONSE, ExampleMessages.format("example.invalid.payload"));
    }
}
