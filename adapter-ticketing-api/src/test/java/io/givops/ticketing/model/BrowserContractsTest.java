package io.givops.ticketing.model;

import static org.assertj.core.api.Assertions.*;
import io.givops.ticketing.model.TicketBrowserModels.*;
import io.givops.ticketing.model.TicketModels.*;
import java.net.URI;
import java.util.*;
import org.junit.jupiter.api.Test;

class BrowserContractsTest {
    final UUID instance = UUID.randomUUID();
    final TicketRef ticket = new TicketRef(instance, "space", "opaque-id");
    final SourceRevision revision = new SourceRevision("git", "opaque-full-object-id", true);
    @Test void preservesHostedConstructionAndRejectsFakeRepositoryUrlAndTraversal() {
        var hosted = new InstanceSource(instance, "hosted", TicketProvider.JIRA_CLOUD, URI.create("https://example.test"), 1);
        assertThat(hosted.sourceKind()).isEqualTo(SourceKind.HOSTED_SERVICE);
        var repository = new RepositorySource(UUID.randomUUID(), "refs/heads/main", "tickets", "config.yml", List.of("assets"), Map.of("storeId", "stable"));
        assertThat(new InstanceSource(instance, "repo", TicketProvider.valueOf("EXAMPLE"), null, 1, repository).sourceKind()).isEqualTo(SourceKind.REPOSITORY_SNAPSHOT);
        assertThatThrownBy(() -> new InstanceSource(instance, "repo", TicketProvider.valueOf("EXAMPLE"), URI.create("https://fake.test"), 1, repository)).isInstanceOf(IllegalArgumentException.class);
        for (String bad : List.of("../escape", "/absolute", "a//b", "a/./b", "a/%2e%2e/b", "a\\b", "a\u0000b"))
            assertThatThrownBy(() -> new RepositorySource(UUID.randomUUID(), "main", bad, "config.yml", List.of(), Map.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RepositorySource(UUID.randomUUID(), "main", "", "config.yml", List.of(), Map.of("x", "a".repeat(4097)))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void enforcesDocumentManifestAndRevisionBoundsWithoutLoggingText() {
        var asset = new AssetRef("asset", "image/png", 10, "sha256:" + "a".repeat(64), "fixture.png");
        var body = new TicketContent(ticket, revision, ContentFormat.MARKDOWN, "PRIVATE-BODY", List.of(asset), List.of());
        assertThat(body.toString()).doesNotContain("PRIVATE-BODY");
        assertThatThrownBy(() -> new TicketContent(ticket, revision, ContentFormat.PLAIN_TEXT, "x".repeat(1024 * 1024 + 1), List.of(), List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TicketContent(ticket, revision, ContentFormat.MARKDOWN, "x", Collections.nCopies(101, asset), List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TicketContent(ticket, revision, ContentFormat.MARKDOWN, "x", List.of(asset, asset), List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TicketContent(ticket, new SourceRevision("remote", "current", false), ContentFormat.MARKDOWN, "x", List.of(asset), List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AssetRef("asset", "text/plain", 1, "git-object-id", "fixture")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AssetRef("asset", "text/plain", -1, "digest", "fixture")).isInstanceOf(IllegalArgumentException.class);
    }
}
