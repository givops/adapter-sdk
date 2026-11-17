package io.givops.ticketing.model;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import io.givops.ticketing.model.TicketBrowserModels.SourceRevision;
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

    public enum SourceKind { HOSTED_SERVICE, REPOSITORY_SNAPSHOT }

    /** Non-secret, inert repository configuration. The host authorizes the exact catalog binding. */
    public record RepositorySource(UUID repositoryId, String ref, String root, String configurationPath,
                                   List<String> approvedAssetRoots, Map<String, String> settings) {
        public RepositorySource {
            java.util.Objects.requireNonNull(repositoryId);
            if (ref == null || ref.isBlank() || ref.length() > 1024) throw new IllegalArgumentException("Invalid repository ref.");
            root = repositoryPath(root, true);
            configurationPath = repositoryPath(configurationPath, false);
            approvedAssetRoots = List.copyOf(approvedAssetRoots);
            if (approvedAssetRoots.size() > 32) throw new IllegalArgumentException("Too many asset roots.");
            approvedAssetRoots.forEach(value -> repositoryPath(value, true));
            settings = Map.copyOf(settings);
            if (settings.size() > 64 || settings.entrySet().stream().anyMatch(entry -> entry.getKey().length() > 128
                    || entry.getValue().length() > 4096 || entry.getKey().codePoints().anyMatch(Character::isISOControl)
                    || entry.getValue().codePoints().anyMatch(Character::isISOControl)))
                throw new IllegalArgumentException("Invalid repository settings.");
        }
        private static String repositoryPath(String path, boolean emptyAllowed) {
            if (path == null || !emptyAllowed && path.isEmpty() || path.length() > 2048 || path.startsWith("/")
                    || path.contains("\\") || path.contains("%") || path.codePoints().anyMatch(Character::isISOControl)
                    || java.util.Arrays.stream(path.split("/", -1)).anyMatch(part -> part.equals(".") || part.equals("..")
                        || part.isEmpty() && !path.isEmpty())) throw new IllegalArgumentException("Invalid repository path.");
            return path;
        }
    }

    public record InstanceSource(UUID instanceId, String instanceKey, TicketProvider provider, URI baseUrl,
                                 long revision, RepositorySource repositorySource) {
        public InstanceSource {
            if (repositorySource != null && baseUrl != null) throw new IllegalArgumentException("Repository sources have no API URL.");
        }
        public InstanceSource(UUID instanceId, String instanceKey, TicketProvider provider, URI baseUrl, long revision) {
            this(instanceId, instanceKey, provider, baseUrl, revision, null);
        }
        public SourceKind sourceKind() { return repositorySource == null ? SourceKind.HOSTED_SERVICE : SourceKind.REPOSITORY_SNAPSHOT; }
    }

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
                                     LocalDate releaseDate, Instant observedAt, String nativeState,
                                     LocalDate startDate, Instant closedAt) {
        public VersionObservation(String externalVersionId, String name, boolean released, boolean archived,
                                  LocalDate releaseDate, Instant observedAt) {
            this(externalVersionId, name, released, archived, releaseDate, observedAt, null, null, null);
        }
    }

    public record IssueIdentity(String externalProjectId, String externalIssueId) { }

    public record IssueObservation(IssueIdentity identity, String issueKey, String summary, String type, String status,
                                   String statusCategory, String priority, String resolution, Instant createdAt,
                                   Instant updatedAt, Instant resolvedAt, List<String> releaseVersionIds,
                                   URI webUrl, Instant observedAt, String assigneeAccountId, @Deprecated String description, List<String> labels,
                                   SourceRevision sourceRevision) {
        public IssueObservation {
            releaseVersionIds = List.copyOf(releaseVersionIds);
            labels = labels == null ? List.of() : List.copyOf(labels);
        }
        /** Legacy construction remains source-compatible. Observation adapters must supply null description. */
        public IssueObservation(IssueIdentity identity, String issueKey, String summary, String type, String status,
                                String statusCategory, String priority, String resolution, Instant createdAt,
                                Instant updatedAt, Instant resolvedAt, List<String> releaseVersionIds,
                                URI webUrl, Instant observedAt, String assigneeAccountId, String description, List<String> labels) {
            this(identity, issueKey, summary, type, status, statusCategory, priority, resolution, createdAt,
                    updatedAt, resolvedAt, releaseVersionIds, webUrl, observedAt, assigneeAccountId, description, labels, null);
        }
        public IssueObservation(IssueIdentity identity, String issueKey, String summary, String type, String status,
                                String statusCategory, String priority, String resolution, Instant createdAt,
                                Instant updatedAt, Instant resolvedAt, List<String> releaseVersionIds,
                                URI webUrl, Instant observedAt, String assigneeAccountId) {
            this(identity, issueKey, summary, type, status, statusCategory, priority, resolution, createdAt,
                    updatedAt, resolvedAt, releaseVersionIds, webUrl, observedAt, assigneeAccountId, null, List.of());
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
