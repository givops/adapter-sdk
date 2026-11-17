package io.givops.ticketing.model;

import io.givops.ticketing.model.TicketModels.InstanceSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Read-only transient browser contracts. Full text and streams must never enter observation persistence. */
public final class TicketBrowserModels {
    private TicketBrowserModels() { }
    public static final String BROWSE = "ticketing.browse";
    public static final String CONTENT = "ticketing.content";
    public static final String ASSETS = "ticketing.assets";
    public static final String NAVIGATION = "ticketing.navigation";
    public record SourceRevision(String kind, String value, boolean immutable) {
        public SourceRevision {
            if (kind == null || !kind.matches("[a-z][a-z0-9-]{0,63}") || value == null || value.isBlank()
                    || value.length() > 1024 || value.codePoints().anyMatch(Character::isISOControl))
                throw new IllegalArgumentException("Invalid source revision.");
        }
    }
    /** External ID is opaque and stable; display keys and repository paths are not identities. */
    public record TicketRef(UUID instanceId, String externalProjectId, String externalIssueId) { }
    public record BrowserRequest(InstanceSource source, UUID ticketProjectId, String externalProjectId,
                                 String projectKey, SourceRevision revision, Instant capturedAt) { }
    public enum Lifecycle { ACTIVE, ARCHIVED, DRAFT }
    /** Cancellation is distinct from a successful completion. Native status remains available separately. */
    public enum Outcome { UNKNOWN, OPEN, IN_PROGRESS, SUCCEEDED, CANCELLED }
    public enum ContentFormat { MARKDOWN, PLAIN_TEXT }
    public record BrowseDescriptor(List<String> queryFields, List<Lifecycle> lifecycles, List<String> sorts,
                                   boolean snapshot, boolean content, boolean assets) {
        public BrowseDescriptor { queryFields = List.copyOf(queryFields); lifecycles = List.copyOf(lifecycles); sorts = List.copyOf(sorts); }
    }
    /** Families combine with AND; values within a family and labels combine with OR. Continuation is provider opaque. */
    public record BrowseQuery(String text, Lifecycle lifecycle, List<String> statuses, List<String> categories,
                              List<String> labels, String sort, int pageSize, String continuation) {
        public BrowseQuery {
            text = text == null ? "" : java.text.Normalizer.normalize(text.strip(), java.text.Normalizer.Form.NFC);
            statuses = statuses == null ? List.of() : List.copyOf(statuses);
            categories = categories == null ? List.of() : List.copyOf(categories);
            labels = labels == null ? List.of() : List.copyOf(labels);
            lifecycle = lifecycle == null ? Lifecycle.ACTIVE : lifecycle;
            sort = sort == null ? "key-asc" : sort;
            if (text.length() > 256 || pageSize < 1 || pageSize > 100 || continuation != null && continuation.length() > 4096
                    || statuses.size() > 100 || categories.size() > 100 || labels.size() > 100)
                throw new IllegalArgumentException("Invalid browse query.");
        }
    }
    public record TicketSummary(TicketRef ref, String displayKey, String title, String nativeStatus, String category,
                                Lifecycle lifecycle, Outcome outcome, List<String> labels, URI webUrl,
                                Instant updatedAt, boolean contentAvailable, SourceRevision revision) {
        public TicketSummary { labels = List.copyOf(labels); }
    }
    public record TicketPage(List<TicketSummary> items, String nextContinuation, Long exactTotal,
                             SourceRevision revision, Instant capturedAt, BrowseQuery effectiveQuery) {
        public TicketPage { items = List.copyOf(items); }
    }
    public record Navigation(URI spaceUrl, URI ticketUrl) { }
    public record TicketDetail(TicketSummary ticket, Instant createdAt, String assigneeId,
                               List<String> releaseVersionIds, Navigation navigation) {
        public TicketDetail { releaseVersionIds = List.copyOf(releaseVersionIds); }
    }
    /** Digest is sha256:<64 lowercase hex> of raw bytes, separate from repository object identity. */
    public record AssetRef(String id, String mediaType, long size, String digest, String filename) {
        public AssetRef {
            if (size < 0 || size > 10L * 1024 * 1024 || digest == null || !digest.matches("sha256:[0-9a-f]{64}")
                    || id == null || id.isBlank() || id.length() > 1024 || id.codePoints().anyMatch(Character::isISOControl))
                throw new IllegalArgumentException("Invalid asset metadata.");
        }
    }
    public record LinkRef(String target, TicketRef ticket, URI externalUrl, String assetId) { }
    public record TicketContent(TicketRef ref, SourceRevision revision, ContentFormat format, String text,
                                List<AssetRef> assets, List<LinkRef> links) {
        public TicketContent {
            java.util.Objects.requireNonNull(ref); java.util.Objects.requireNonNull(revision);
            java.util.Objects.requireNonNull(format); java.util.Objects.requireNonNull(text);
            assets = List.copyOf(assets); links = List.copyOf(links);
            if (text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 1024 * 1024 || assets.size() + links.size() > 100)
                throw new IllegalArgumentException("Content exceeds SDK bounds.");
            if (!assets.isEmpty() && !revision.immutable()) throw new IllegalArgumentException("Assets require immutable content.");
            if (assets.stream().map(AssetRef::id).distinct().count() != assets.size()) throw new IllegalArgumentException("Duplicate asset identity.");
        }
        @Override public String toString() { return "TicketContent[redacted]"; }
    }
    public interface TicketAsset extends AutoCloseable {
        TicketRef ref();
        SourceRevision revision();
        AssetRef metadata();
        InputStream stream();
        @Override void close() throws IOException;
    }
}
