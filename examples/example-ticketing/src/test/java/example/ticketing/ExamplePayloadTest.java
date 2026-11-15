package example.ticketing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import io.givops.ticketing.model.TicketException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExamplePayloadTest {
    @TempDir Path directory;
    @Test void independentlyChangedPayloadRequiresAdapterMappingRepair() throws Exception {
        Path fixture = directory.resolve("response.json");
        var original = new ExamplePayload("name");
        Files.writeString(fixture, "{\"name\":\"Original synthetic project\"}");
        assertThat(original.name(fixture)).isEqualTo("Original synthetic project");
        Files.writeString(fixture, "{\"displayName\":\"Changed synthetic project\"}");
        assertThatThrownBy(() -> original.name(fixture)).isInstanceOfSatisfying(TicketException.class,
                failure -> assertThat(failure.code()).isEqualTo(TicketException.Code.INVALID_RESPONSE));
        assertThat(new ExamplePayload("displayName").name(fixture)).isEqualTo("Changed synthetic project");
    }
    @Test void rejectsUnboundedOrMalformedSyntheticResponses() throws Exception {
        Path fixture = directory.resolve("response.json");
        var mapper = new ExamplePayload("name");
        for (String input : new String[]{"not-json", "{\"name\":42}", "{\"name\":\"\"}", "x".repeat(4097)}) {
            Files.writeString(fixture, input);
            assertThatThrownBy(() -> mapper.name(fixture)).isInstanceOf(TicketException.class);
        }
    }
}
