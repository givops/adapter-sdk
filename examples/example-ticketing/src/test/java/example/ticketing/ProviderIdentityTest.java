package example.ticketing;
import static org.assertj.core.api.Assertions.*;
import io.givops.ticketing.model.TicketModels.TicketProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
class ProviderIdentityTest {
    @Test void oldCommandJsonLeavesCapturedVersionNamesUnknown() {
        var mapper = JsonMapper.builder().build();
        var command = mapper.readValue("""
                {"id":"019aad3e-bd09-7799-a6b8-44efee070d73","projectId":null,"definitionId":null,
                 "action":"VERSION_ONLY","state":"SUCCEEDED","createdAt":null,"completedAt":null,
                 "versionId":"provider-id","issueKey":null,"browseUrl":null,"summary":null,
                 "errorCode":null,"refreshRequired":false}
                """, io.givops.release.commands.ReleaseCommandModels.Command.class);
        assertThat(command.versionName()).isNull();
        assertThat(command.providerVersionName()).isNull();
        assertThat(command.versionId()).isEqualTo("provider-id");
    }
    @Test void arbitraryProviderIdsRoundTripAsStringsAndMapKeysWithoutGlobalRetention() {
        var mapper = JsonMapper.builder().build();
        var first = TicketProvider.valueOf("CUSTOM_17");
        var second = TicketProvider.valueOf("CUSTOM_17");
        assertThat(first).isEqualTo(second).isNotSameAs(second);
        assertThat(TicketProvider.valueOf("JIRA_CLOUD")).isSameAs(TicketProvider.JIRA_CLOUD);
        assertThat(mapper.writeValueAsString(first)).isEqualTo("\"CUSTOM_17\"");
        assertThat(mapper.readValue("\"CUSTOM_17\"", TicketProvider.class)).isEqualTo(first);
        String json = mapper.writeValueAsString(Map.of(first, "value"));
        assertThat(json).isEqualTo("{\"CUSTOM_17\":\"value\"}");
        assertThat(mapper.readValue(json, new TypeReference<Map<TicketProvider, String>>() {})).containsEntry(second, "value");
        assertThatThrownBy(() -> TicketProvider.valueOf("bad identity")).isInstanceOf(IllegalArgumentException.class);
    }
}
