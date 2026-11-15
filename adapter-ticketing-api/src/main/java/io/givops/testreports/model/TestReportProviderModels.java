package io.givops.testreports.model;

import io.givops.ticketing.model.TicketModels.InstanceSource;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Provider-neutral immutable observations; provider JSON never crosses this boundary. */
public final class TestReportProviderModels {
    private TestReportProviderModels() { }
    public enum Phase { PREPARING, REQUESTED, TESTING, PASSED, FAILED, ABANDONED, UNKNOWN }
    public enum Extractor { PROJECT_ID, PROJECT_KEY, STRING, OPTION_ID, VERSION_IDS }
    public enum HistoryCoverage { COMPLETE, INCOMPLETE, UNAVAILABLE }
    public enum FieldState { PRESENT, MISSING, UNAVAILABLE, INVALID, AMBIGUOUS }
    public enum Cardinality { ONE, MANY }
    @Schema(name="TestReportFieldRef")
    public record FieldRef(String fieldId, Extractor extractor) { }
    @Schema(name="TestReportFieldMapping")
    public record FieldMapping(FieldRef targetProject, FieldRef versions, FieldRef family) { }
    @Schema(name="TestReportStatusRule")
    public record StatusRule(String statusId, Phase phase, boolean active) { }
    @Schema(name="TestReportSourceProject")
    public record SourceProject(UUID ticketProjectId, String externalProjectId, String projectKey) { }
    @Schema(name="TestReportOrdinaryProject")
    public record OrdinaryProject(UUID projectId, UUID ticketProjectId, String externalProjectId, String projectKey) { }
    @Schema(name="TestReportReportInput")
    public record ReportInput(InstanceSource source, List<SourceProject> sourceProjects, Set<String> issueTypeIds,
                              FieldMapping mapping, List<StatusRule> statuses, List<OrdinaryProject> ordinaryProjects) {
        public ReportInput { sourceProjects = List.copyOf(sourceProjects); issueTypeIds = Set.copyOf(issueTypeIds);
            statuses = List.copyOf(statuses); ordinaryProjects = List.copyOf(ordinaryProjects); }
    }
    @Schema(name="TestReportNamedId")
    public record NamedId(String id, String name) { }
    @Schema(name="TestReportDiagnostic")
    public record Diagnostic(String code, String fieldId, String message) { }
    @Schema(name="TestReportTypedValue")
    public record TypedValue(Extractor kind, String value, String label) { }
    @Schema(name="TestReportVersionValue")
    public record VersionValue(UUID ticketProjectId, String externalVersionId, String name) { }
    @Schema(name="TestReportAssignee")
    public record Assignee(String accountId, String displayName) { }
    @Schema(name="TestReportStatusEvent")
    public record StatusEvent(String id, Instant at, String fromStatusId, String fromStatusName,
                              String toStatusId, String toStatusName) { }
    @Schema(name="TestReportHistory")
    public record History(HistoryCoverage coverage, List<StatusEvent> events, List<Diagnostic> diagnostics) {
        public History { events = List.copyOf(events); diagnostics = List.copyOf(diagnostics); }
    }
    @Schema(name="TestReportRawReport")
    public record RawReport(String externalIssueId, String issueKey, String browseUrl, UUID sourceTicketProjectId,
                            String summary, NamedId issueType, NamedId status, Assignee assignee,
                            Instant createdAt, Instant updatedAt, TypedValue target, FieldState targetState,
                            List<VersionValue> versions, FieldState versionsState, TypedValue family,
                            FieldState familyState, History history, List<Diagnostic> diagnostics) {
        public RawReport { versions = List.copyOf(versions); diagnostics = List.copyOf(diagnostics); }
    }
    @Schema(name="TestReportReportDataset")
    public record ReportDataset(Instant observedAt, List<RawReport> reports) {
        public ReportDataset { reports = List.copyOf(reports); }
    }
    @Schema(name="TestReportOrdinaryVersion")
    public record OrdinaryVersion(UUID ticketProjectId, String externalVersionId, String name,
                                  boolean released, boolean archived, LocalDate releaseDate) { }
    @Schema(name="TestReportOrdinaryIssue")
    public record OrdinaryIssue(UUID ticketProjectId, String externalIssueId, String issueKey, String browseUrl,
                                String summary, NamedId issueType, NamedId status, String statusCategory,
                                Assignee assignee, Instant createdAt, Instant updatedAt, List<String> versionIds,
                                TypedValue family, FieldState familyState, History history) {
        public OrdinaryIssue { versionIds = List.copyOf(versionIds); }
    }
    @Schema(name="TestReportOrdinaryDataset")
    public record OrdinaryDataset(Instant observedAt, List<OrdinaryVersion> versions, List<OrdinaryIssue> issues) {
        public OrdinaryDataset { versions = List.copyOf(versions); issues = List.copyOf(issues); }
    }
    @Schema(name="TestReportFieldMetadata")
    public record FieldMetadata(String id, String name, String schemaType, String itemType,
                                Cardinality cardinality, List<Extractor> supportedExtractors) {
        public FieldMetadata { supportedExtractors = List.copyOf(supportedExtractors); }
    }
    @Schema(name="TestReportProjectMetadata")
    public record ProjectMetadata(UUID ticketProjectId, List<NamedId> issueTypes, List<NamedId> statuses) {
        public ProjectMetadata { issueTypes = List.copyOf(issueTypes); statuses = List.copyOf(statuses); }
    }
    @Schema(name="TestReportMetadataSnapshot")
    public record MetadataSnapshot(Instant observedAt, List<FieldMetadata> fields, List<ProjectMetadata> projects,
                                   List<Diagnostic> diagnostics) {
        public MetadataSnapshot { fields = List.copyOf(fields); projects = List.copyOf(projects);
            diagnostics = List.copyOf(diagnostics); }
    }
    @Schema(name="TestReportProbeResult")
    public record ProbeResult(Instant observedAt, int sampledCount, boolean valid, List<RawReport> samples,
                              List<Diagnostic> diagnostics) {
        public ProbeResult { samples = List.copyOf(samples); diagnostics = List.copyOf(diagnostics); }
    }
}
