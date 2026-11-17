package io.givops.ticketing.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import io.givops.ticketing.model.TicketWriteModels.CreateIssueCommand;
import io.givops.ticketing.model.TicketWriteModels.VersionSelector;
import io.givops.ticketing.model.TicketWriteModels.VersionSelectorKind;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TicketWriteModelsTest {
    @Test void preservesLegacyVersionsAndDistinguishesNumericNamesFromIds() {
        var legacy = new CreateIssueCommand("Issue", null, null, null, null, null, null, null,
                List.of("123"), false, null);
        assertThat(legacy.fixVersions()).containsExactly("123");
        assertThat(legacy.versionSelectors()).isEmpty();
        var selectors = new ArrayList<>(List.of(new VersionSelector(VersionSelectorKind.NAME, "123"),
                new VersionSelector(VersionSelectorKind.EXTERNAL_ID, "123")));
        var explicit = new CreateIssueCommand("Issue", null, null, null, null, null, null, null,
                null, false, null, selectors);
        selectors.clear();
        assertThat(explicit.versionSelectors()).hasSize(2);
        assertThat(explicit.versionSelectors().get(0)).isNotEqualTo(explicit.versionSelectors().get(1));
        assertThatIllegalArgumentException().isThrownBy(() -> new CreateIssueCommand("Issue", null, null, null,
                null, null, null, null, List.of("old"), false, null, explicit.versionSelectors()));
        assertThatIllegalArgumentException().isThrownBy(() -> new VersionSelector(null, "123"));
        assertThatIllegalArgumentException().isThrownBy(() -> new VersionSelector(VersionSelectorKind.NAME, " "));
    }
}
