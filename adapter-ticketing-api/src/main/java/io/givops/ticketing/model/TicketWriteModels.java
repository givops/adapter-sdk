package io.givops.ticketing.model;

import io.givops.ticketing.model.TicketModels.ProjectObservation;
import io.givops.adapters.i18n.SdkMessages;
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
            String leaderExternalId,
            String projectTemplateKey) {
        public CreateProjectCommand(String projectKey, String displayName, String description, String leaderExternalId) {
            this(projectKey, displayName, description, leaderExternalId, null);
        }
    }

    public record RemoteProjectRef(java.util.UUID id, java.util.UUID instanceId, String externalProjectId,
                                   String projectKey, String displayName, long revision) { }

    public record CreateVersionCommand(
            String name,
            boolean released,
            boolean archived,
            LocalDate releaseDate) { }

    /** Explicit selectors avoid interpreting a numeric version name as a provider ID. */
    public enum VersionSelectorKind { NAME, EXTERNAL_ID }
    public record VersionSelector(VersionSelectorKind kind, String value) {
        public VersionSelector {
            if (kind == null || value == null || value.isBlank() || value.length() > 255) {
                throw new IllegalArgumentException(SdkMessages.format("ticketing.write.selector.invalid"));
            }
        }
    }

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
            String assigneeExternalId,
            List<VersionSelector> versionSelectors) {
        public CreateIssueCommand(String summary, String description, IssueKind type, String providerType,
                WorkflowState workflowState, String providerStatus, PriorityLevel priority, String providerPriority,
                List<String> fixVersions, boolean assignToSelf, String assigneeExternalId) {
            this(summary, description, type, providerType, workflowState, providerStatus, priority, providerPriority,
                    fixVersions, assignToSelf, assigneeExternalId, List.of());
        }
        public CreateIssueCommand {
            fixVersions = fixVersions == null ? List.of() : List.copyOf(fixVersions);
            versionSelectors = versionSelectors == null ? List.of() : List.copyOf(versionSelectors);
            if (!fixVersions.isEmpty() && !versionSelectors.isEmpty()) {
                throw new IllegalArgumentException(SdkMessages.format("ticketing.write.selector.mixed"));
            }
        }
    }
}
