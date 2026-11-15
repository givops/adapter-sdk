package io.givops.ticketing.model;

import io.givops.ticketing.model.TicketModels.ProjectObservation;
import java.time.LocalDate;
import java.util.List;

/** Provider-agnostic commands and outcomes for creating remote ticketing resources. */
public final class TicketWriteModels {
    private TicketWriteModels() { }

    public enum IssueKind { BUG, FEATURE, TASK, EPIC }

    public enum WorkflowState { OPEN, IN_PROGRESS, DONE }

    public enum PriorityLevel { CRITICAL, HIGH, NORMAL, LOW }

    public record CreateProjectCommand(
            String projectKey,
            String displayName,
            String description,
            String leaderExternalId) { }

    public record RemoteProjectRef(java.util.UUID id, java.util.UUID instanceId, String externalProjectId,
                                   String projectKey, String displayName, long revision) { }

    public record CreateVersionCommand(
            String name,
            boolean released,
            boolean archived,
            LocalDate releaseDate) { }

    public record CreateIssueCommand(
            String summary,
            String description,
            IssueKind type,
            String providerType,
            WorkflowState workflowState,
            String providerStatus,
            PriorityLevel priority,
            String providerPriority,
            List<String> fixVersions,
            boolean assignToSelf,
            String assigneeExternalId) {
        public CreateIssueCommand {
            fixVersions = fixVersions == null ? List.of() : List.copyOf(fixVersions);
        }
    }
}
