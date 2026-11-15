package io.givops.forge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.givops.catalog.ForgeProvider;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Provider-neutral observations. Unknown provider fields remain null. */
public final class ForgeModels {
    private ForgeModels() {}

    public record Settings(UUID repositoryId, long revision, boolean enabled, int historyDays, int commitLimit) {}
    public record Source(UUID repositoryId, UUID forgeId, String forgeKey, ForgeProvider provider,
                         URI baseUrl, long forgeRevision, String projectId, String repositoryPath, long repositoryRevision) {
        public Source(UUID repositoryId, UUID forgeId, String forgeKey, ForgeProvider provider,
                      URI baseUrl, long forgeRevision, String projectId, String repositoryPath) {
            this(repositoryId, forgeId, forgeKey, provider, baseUrl, forgeRevision, projectId, repositoryPath, 0);
        }
        public Source(UUID repositoryId, UUID forgeId, String forgeKey, ForgeProvider provider,
                      URI baseUrl, long forgeRevision, String projectId) {
            this(repositoryId, forgeId, forgeKey, provider, baseUrl, forgeRevision, projectId, null);
        }
    }
    public record Branch(String ref, String sha) {}
    public record Selection(UUID successfulScanId, Instant scmObservedAt, Instant scmSynchronizedAt,
                            Instant capturedAt, Instant windowStart, List<String> commitShas, List<Branch> branches) {
        public Selection { commitShas = List.copyOf(commitShas); branches = List.copyOf(branches); }
    }
    public record RefreshInput(Source source, Settings settings, Selection selection,
                               Map<String, CachedAssociations> cachedAssociations) {
        public RefreshInput { cachedAssociations = Map.copyOf(cachedAssociations); }
    }
    public record ChangeRequestKey(@JsonProperty("projectId") String providerRepositoryId,
                                   @JsonProperty("iid") String localId) {}
    public record PipelineKey(String projectId, String id) {}
    public record Person(String id, String username, String name) {}
    public enum PipelineCoverageState { OBSERVED, UNKNOWN }
    public record PipelineCoverage(PipelineCoverageState state, String evidence) {
        private static final PipelineCoverage NOT_OBSERVED = new PipelineCoverage(PipelineCoverageState.UNKNOWN,
                "not_observed");
    }
    public record ActionsMetadata(String workflowId, String workflowName, int runAttempt,
                                  String status, String conclusion, String event, String headRepositoryId) {}
    public record Pipeline(PipelineKey key, String status, String ref, String sha, String source, String webUrl,
                           Instant createdAt, Instant updatedAt, Instant observedAt, ActionsMetadata actions) {
        public Pipeline(PipelineKey key, String status, String ref, String sha, String source, String webUrl,
                        Instant createdAt, Instant updatedAt, Instant observedAt) {
            this(key, status, ref, sha, source, webUrl, createdAt, updatedAt, observedAt, null);
        }
    }
    public enum CiState { SUCCESS, FAILED, PENDING, UNKNOWN }
    public record CiSummary(CiState state, PipelineCoverageState coverage, String evidence) {}
    public enum ConflictState { CLEAR, CONFLICT, UNKNOWN }
    public enum ReviewerCoverage { COMPLETE, TEAM_REQUESTED, UNKNOWN }
    public record TaskSignals(Integer unresolvedDiscussionCount, Set<String> approvedProviderIds,
                              boolean approvalsKnown, String mergeStatus, CiSummary ciSummary,
                              ConflictState conflictState, ReviewerCoverage reviewerCoverage) {
        public TaskSignals {
            approvedProviderIds = approvedProviderIds == null ? Set.of() : Set.copyOf(approvedProviderIds);
            ciSummary = ciSummary == null ? new CiSummary(CiState.UNKNOWN, PipelineCoverageState.UNKNOWN,
                    "not_observed") : ciSummary;
            conflictState = conflictState == null ? ConflictState.UNKNOWN : conflictState;
            reviewerCoverage = reviewerCoverage == null ? ReviewerCoverage.UNKNOWN : reviewerCoverage;
        }

        public TaskSignals(Integer unresolvedDiscussionCount, Set<String> approvedProviderIds,
                           boolean approvalsKnown, String mergeStatus) {
            this(unresolvedDiscussionCount, approvedProviderIds, approvalsKnown, mergeStatus, null, null, null);
        }
    }
    public record ChangeRequest(ChangeRequestKey key, String providerId, String webUrl, String title, String description,
                               String state, boolean draft, String sourceProjectId, String targetProjectId,
                               String sourceBranch, String targetBranch, String sha, String mergeCommitSha,
                               String squashCommitSha, Person author, List<Person> assignees, List<Person> reviewers,
                               List<String> labels, Instant createdAt, Instant updatedAt, Instant mergedAt,
                               Instant closedAt, Pipeline headPipeline, Instant observedAt, TaskSignals taskSignals,
                               ChangeRequestLifecycle lifecycle, List<Pipeline> pipelines,
                               List<Pipeline> pipelineCandidates, PipelineCoverage pipelineCoverage) {
        public ChangeRequest {
            assignees = List.copyOf(assignees); reviewers = List.copyOf(reviewers); labels = List.copyOf(labels);
            pipelines = pipelines == null ? List.of() : List.copyOf(pipelines);
            pipelineCandidates = pipelineCandidates == null ? List.of() : List.copyOf(pipelineCandidates);
            pipelineCoverage = pipelineCoverage == null ? PipelineCoverage.NOT_OBSERVED : pipelineCoverage;
        }

        public ChangeRequest(ChangeRequestKey key, String providerId, String webUrl, String title, String description,
                             String state, boolean draft, String sourceProjectId, String targetProjectId,
                             String sourceBranch, String targetBranch, String sha, String mergeCommitSha,
                             String squashCommitSha, Person author, List<Person> assignees, List<Person> reviewers,
                             List<String> labels, Instant createdAt, Instant updatedAt, Instant mergedAt,
                             Instant closedAt, Pipeline headPipeline, Instant observedAt, TaskSignals taskSignals) {
            this(key, providerId, webUrl, title, description, state, draft, sourceProjectId, targetProjectId,
                    sourceBranch, targetBranch, sha, mergeCommitSha, squashCommitSha, author, assignees, reviewers,
                    labels, createdAt, updatedAt, mergedAt, closedAt, headPipeline, observedAt, taskSignals,
                    null, null, null, null);
        }

        public ChangeRequest(ChangeRequestKey key, String providerId, String webUrl, String title, String description,
                             String state, boolean draft, String sourceProjectId, String targetProjectId,
                             String sourceBranch, String targetBranch, String sha, String mergeCommitSha,
                             String squashCommitSha, Person author, List<Person> assignees, List<Person> reviewers,
                             List<String> labels, Instant createdAt, Instant updatedAt, Instant mergedAt,
                             Instant closedAt, Pipeline headPipeline, Instant observedAt) {
            this(key, providerId, webUrl, title, description, state, draft, sourceProjectId, targetProjectId,
                    sourceBranch, targetBranch, sha, mergeCommitSha, squashCommitSha, author, assignees, reviewers,
                    labels, createdAt, updatedAt, mergedAt, closedAt, headPipeline, observedAt, null);
        }

        public ChangeRequest withTaskSignals(TaskSignals signals) {
            return new ChangeRequest(key, providerId, webUrl, title, description, state, draft, sourceProjectId,
                    targetProjectId, sourceBranch, targetBranch, sha, mergeCommitSha, squashCommitSha, author,
                    assignees, reviewers, labels, createdAt, updatedAt, mergedAt, closedAt, headPipeline, observedAt,
                    signals, lifecycle, pipelines, pipelineCandidates, pipelineCoverage);
        }
    }
    public record TaskObservations(Source source, List<ChangeRequest> items, UUID generationId, Instant observedAt,
                                   boolean stale, String unavailableReason) {
        public TaskObservations { items = List.copyOf(items); }
    }
    public record CommitAssociations(String sha, @JsonProperty("mergeRequests") List<ChangeRequestKey> changeRequests,
                                     Instant observedAt) {
        public CommitAssociations { changeRequests = List.copyOf(changeRequests); }
    }
    public record CachedAssociations(@JsonProperty("mergeRequests") List<ChangeRequest> changeRequests,
                                     Instant observedAt) {
        public CachedAssociations { changeRequests = List.copyOf(changeRequests); }
    }
    public record BranchPipeline(String ref, String sha, Pipeline pipeline, List<Pipeline> pipelines,
                                 List<Pipeline> pipelineCandidates, PipelineCoverage pipelineCoverage) {
        public BranchPipeline {
            pipelines = pipelines == null ? List.of() : List.copyOf(pipelines);
            pipelineCandidates = pipelineCandidates == null ? List.of() : List.copyOf(pipelineCandidates);
            pipelineCoverage = pipelineCoverage == null ? PipelineCoverage.NOT_OBSERVED : pipelineCoverage;
        }

        public BranchPipeline(String ref, String sha, Pipeline pipeline) {
            this(ref, sha, pipeline, null, null, null);
        }
    }
    public enum CapabilityState { AVAILABLE, UNSUPPORTED, UNKNOWN, DISABLED, FORBIDDEN, TEMPORARILY_UNAVAILABLE }
    public record Capability(CapabilityState state, String evidence, Instant observedAt) {}
    public record Capabilities(String vendor, String version, String edition, String hosting, String tier,
                               Map<String, Capability> operations, Instant observedAt) {
        public Capabilities { operations = Map.copyOf(operations); }
    }
    public record Snapshot(Capabilities capabilities, @JsonProperty("mergeRequests") List<ChangeRequest> changeRequests,
                           List<CommitAssociations> associations, List<BranchPipeline> branchPipelines) {
        public Snapshot {
            changeRequests = List.copyOf(changeRequests); associations = List.copyOf(associations);
            branchPipelines = List.copyOf(branchPipelines);
        }
    }
    public enum RefreshState { RUNNING, SUCCEEDED, FAILED }
    public record Refresh(UUID id, UUID repositoryId, RefreshState status, Instant startedAt,
                          Instant completedAt, String errorCode) {}
    public record Status(UUID repositoryId, Settings settings, Refresh latestRefresh, UUID generationId,
                         Instant observedAt, UUID successfulScanId, UUID currentSuccessfulScanId,
                         Instant windowStart, Instant windowEnd, int selectedCommitCount, boolean stale) {}
    public record Detail<T>(T item, UUID generationId, boolean stale) {}
    public record Page<T>(List<T> items, String nextCursor, UUID generationId, boolean stale) {
        public Page { items = List.copyOf(items); }
    }
    public enum AssociationCoverage { OBSERVED, NOT_OBSERVED }
    public record AssociationPage(List<ChangeRequest> items, String nextCursor, UUID generationId, boolean stale,
                                  AssociationCoverage coverage, Instant observedAt) {
        public AssociationPage { items = List.copyOf(items); }
    }
}
