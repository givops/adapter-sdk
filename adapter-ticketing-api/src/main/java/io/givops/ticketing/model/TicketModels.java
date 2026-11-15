package io.givops.ticketing.model;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class TicketModels {
    private TicketModels() { }

    /** Open provider identity. Unknown values have value equality and are never retained globally. */
    public static final class TicketProvider implements Comparable<TicketProvider> {
        public static final TicketProvider JIRA_CLOUD = new TicketProvider("JIRA_CLOUD");
        public static final TicketProvider YOUTRACK = new TicketProvider("YOUTRACK");
        public static final TicketProvider GITLAB = new TicketProvider("GITLAB");
        public static final TicketProvider GITHUB = new TicketProvider("GITHUB");
        private final String name;
        private TicketProvider(String name) { this.name = name; }
        @com.fasterxml.jackson.annotation.JsonCreator
        public static TicketProvider valueOf(String name) {
            if (name == null || !name.matches("[A-Z][A-Z0-9_]{0,63}")) throw new IllegalArgumentException(io.givops.adapters.i18n.SdkMessages.format("sdk.provider.identity.invalid"));
            return switch (name) {
                case "JIRA_CLOUD" -> JIRA_CLOUD;
                case "YOUTRACK" -> YOUTRACK;
                case "GITLAB" -> GITLAB;
                case "GITHUB" -> GITHUB;
                default -> new TicketProvider(name);
            };
        }
        @com.fasterxml.jackson.annotation.JsonValue public String name() { return name; }
        @Override public String toString() { return name; }
        @Override public int compareTo(TicketProvider other) { return name.compareTo(other.name); }
        @Override public boolean equals(Object other) { return other instanceof TicketProvider provider && name.equals(provider.name); }
        @Override public int hashCode() { return name.hashCode(); }
    }

    public record InstanceSource(UUID instanceId, String instanceKey, TicketProvider provider, URI baseUrl,
                                 long revision) { }

    public record ProjectQuery(UUID ticketProjectId, String externalProjectId, String projectKey,
                               Set<String> issueKeys, String releaseVersionName) {
        public ProjectQuery {
            issueKeys = Set.copyOf(issueKeys);
        }
    }

    public record ProviderInput(InstanceSource source, List<ProjectQuery> projects, Instant capturedAt) {
        public ProviderInput {
            projects = List.copyOf(projects);
        }
    }

    public record ProjectObservation(String externalProjectId, String projectKey, String displayName,
                                     URI webUrl, Instant observedAt) { }

    public record VersionObservation(String externalVersionId, String name, boolean released, boolean archived,
                                     LocalDate releaseDate, Instant observedAt) { }

    public record IssueIdentity(String externalProjectId, String externalIssueId) { }

    public record IssueObservation(IssueIdentity identity, String issueKey, String summary, String type, String status,
                                   String statusCategory, String priority, String resolution, Instant createdAt,
                                   Instant updatedAt, Instant resolvedAt, List<String> releaseVersionIds,
                                   URI webUrl, Instant observedAt, String assigneeAccountId) {
        public IssueObservation {
            releaseVersionIds = List.copyOf(releaseVersionIds);
        }
        public IssueObservation(IssueIdentity identity, String issueKey, String summary, String type, String status,
                                String statusCategory, String priority, String resolution, Instant createdAt,
                                Instant updatedAt, Instant resolvedAt, List<String> releaseVersionIds,
                                URI webUrl, Instant observedAt) {
            this(identity, issueKey, summary, type, status, statusCategory, priority, resolution, createdAt,
                    updatedAt, resolvedAt, releaseVersionIds, webUrl, observedAt, null);
        }
    }

    public enum ReleaseMappingState { NOT_REQUESTED, MATCHED, NOT_FOUND, AMBIGUOUS, UNSUPPORTED }

    public record ReleaseMembership(ReleaseMappingState state, String externalVersionId, List<IssueIdentity> issues,
                                    Instant observedAt) {
        public ReleaseMembership {
            issues = List.copyOf(issues);
        }
    }

    public record ProjectResult(UUID ticketProjectId, ProjectObservation project, List<VersionObservation> versions,
                                List<IssueObservation> issues, Set<String> unresolvedKeys,
                                ReleaseMembership releaseMembership, List<IssueIdentity> assignedIssues) {
        public ProjectResult {
            versions = List.copyOf(versions);
            issues = List.copyOf(issues);
            unresolvedKeys = Set.copyOf(unresolvedKeys);
            if (assignedIssues != null) assignedIssues = List.copyOf(assignedIssues);
        }
        public ProjectResult(UUID ticketProjectId, ProjectObservation project, List<VersionObservation> versions,
                             List<IssueObservation> issues, Set<String> unresolvedKeys, ReleaseMembership releaseMembership) {
            this(ticketProjectId, project, versions, issues, unresolvedKeys, releaseMembership, null);
        }
    }

    public record ProviderSnapshot(List<ProjectResult> projects) {
        public ProviderSnapshot {
            projects = List.copyOf(projects);
        }
    }
}
