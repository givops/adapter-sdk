package io.givops.release.commands;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

/** Explicit, provider-neutral contracts for release version and test report commands. */
public final class ReleaseCommandModels {
    private ReleaseCommandModels() { }
    @Schema(name = "ReleaseReportKind")
    public enum Kind { CAMPAIGN, HOTFIX }
    @Schema(name = "ReleaseJiraAction")
    public enum Action { VERSION_ONLY, CREATE_REPORT }
    @Schema(name = "ReleaseJiraVersionShape")
    public enum VersionShape { STRING, VERSION_IDS }
    @Schema(name = "ReleaseJiraCapability")
    public record Capability(String state, String reason) { }
    @Schema(name = "ReleaseJiraTicketPreview")
    public record Ticket(String key, String summary, String browseUrl) { }
    @Schema(name = "ReleaseJiraExistingReport")
    public record ExistingReport(String issueKey, String summary, String browseUrl) { }
    @Schema(name = "ReleaseJiraCreationForm")
    public record Form(Capability capability, String context, UUID definitionId, UUID ticketProjectId,
                       String versionName, Kind kind, Action action, String assigneeAccountId,
                       String destinationProjectKey, List<Ticket> tickets, String ticketsState, ExistingReport existingReport,
                       String description, String providerVersionName) {
        public Form(Capability capability, String context, UUID definitionId, UUID ticketProjectId,
                    String versionName, Kind kind, Action action, String assigneeAccountId,
                    String destinationProjectKey, List<Ticket> tickets, String ticketsState, ExistingReport existingReport,
                    String description) {
            this(capability, context, definitionId, ticketProjectId, versionName, kind, action, assigneeAccountId,
                    destinationProjectKey, tickets, ticketsState, existingReport, description, versionName);
        }
        public Form { tickets = List.copyOf(tickets); }
    }
    @Schema(name = "ReleaseJiraCommandRequest")
    public record Request(UUID idempotencyKey, String context, UUID definitionId, UUID ticketProjectId,
                          String versionName, Kind kind, Action action, String summary, LocalDate plannedDate,
                          LocalDate desiredDeliveryDate, String description) {
        public Request { action = action == null ? Action.CREATE_REPORT : action; }
        @Override public String toString() { return "ReleaseCommandRequest[redacted]"; }
    }
    @Schema(name = "ReleaseJiraCommand")
    public record Command(UUID id, UUID projectId, UUID definitionId, Action action, String state,
                          Instant createdAt, Instant completedAt, String versionId, String issueKey,
                          String browseUrl, String summary, String errorCode, boolean refreshRequired,
                          String versionName, String providerVersionName) {
        /** Legacy receipts have no captured version names; do not infer a native target from current configuration. */
        public Command(UUID id, UUID projectId, UUID definitionId, Action action, String state,
                       Instant createdAt, Instant completedAt, String versionId, String issueKey,
                       String browseUrl, String summary, String errorCode, boolean refreshRequired) {
            this(id, projectId, definitionId, action, state, createdAt, completedAt, versionId, issueKey,
                    browseUrl, summary, errorCode, refreshRequired, null, null);
        }
    }
    @Schema(name = "ReleaseReportCreationConfiguration")
    public record Configuration(long revision, boolean enabled, UUID ordinaryTicketProjectId,
                                UUID destinationTicketProjectId, String campaignIssueTypeId, String hotfixIssueTypeId,
                                String targetProjectFieldId, String targetProjectValue, String versionFieldId,
                                VersionShape versionShape, String plannedDateFieldId, String desiredDeliveryDateFieldId) {
        public Configuration { versionShape = versionShape == null ? VersionShape.STRING : versionShape; }
        public static Configuration disabled() {
            return new Configuration(0, false, null, null, null, null, null, null, null, null, null, null);
        }
    }
    @Schema(name = "ReleaseHistoryStatusMapping")
    public record StatusMapping(long revision, List<String> testOkStatuses, List<String> toTestStatuses) {
        public StatusMapping {
            testOkStatuses = testOkStatuses == null ? List.of() : List.copyOf(testOkStatuses);
            toTestStatuses = toTestStatuses == null ? List.of() : List.copyOf(toTestStatuses);
        }
    }
    public record Identity(String accountId, long revision) { }
    public record TicketProject(UUID id, UUID instanceId, String externalId, String key, long revision) { }
    public record Outcome(String versionId, ExistingReport report, String errorCode, boolean uncertain) {
        public static Outcome failure(String versionId, String code, boolean uncertain) {
            return new Outcome(versionId, null, code, uncertain);
        }
    }
}
