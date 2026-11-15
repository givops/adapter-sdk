package io.givops.forge.discovery;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public final class DiscoveryModels {
    private DiscoveryModels() {}

    public record Namespace(String externalId, String kind, String name, String fullPath) {}
    public record RemoteGroup(String externalId, String name, String fullPath, String parentExternalId,
                              String suggestedKey) {}
    public record RemoteProject(String externalId, String name, String fullPath, String cloneUrl, String webUrl,
                                Namespace namespace, String sourceFingerprint, String suggestedKey) {}
    public record LocalProject(UUID repositoryId, UUID projectId, String projectKey, String status) {}
    @Schema(name = "ForgeDiscoveryProject")
    public record Project(RemoteProject source,
                          @Schema(types = {"object", "null"}) LocalProject existing,
                          @Schema(types = {"string", "null"}) String importConflict) {}
    public record Page<T>(@Schema(requiredMode = RequiredMode.REQUIRED) List<T> items,
                          @Schema(requiredMode = RequiredMode.REQUIRED, types = {"string", "null"}) String nextCursor,
                          @Schema(requiredMode = RequiredMode.REQUIRED, pattern = "[a-f0-9]{64}") String context) {
        public Page { items = List.copyOf(items); }
    }
    public record RemotePage<T>(List<T> items, boolean hasNext, long lastId) {
        public RemotePage { items = List.copyOf(items); }
    }
    public record Server(URI baseUrl) {}
    public enum GroupMode { NONE, CREATE, EXISTING }
    public record GroupChoice(@NotNull GroupMode mode, String namespaceId, String key, UUID groupId,
                              Long revision, Boolean acknowledgeExistingAccess) {}
    public record ImportItem(@NotNull UUID itemId,
                             @NotBlank @Pattern(regexp = "[1-9][0-9]{0,18}") String externalId,
                             @NotBlank @Pattern(regexp = "[a-f0-9]{64}") String sourceFingerprint,
                             @NotBlank @Pattern(regexp = "[a-z][a-z0-9-]{0,79}") String projectKey,
                             @NotBlank @Size(max = 200) String displayName,
                             @NotNull Boolean conventionalCommitsEnabled,
                             @NotNull @Valid GroupChoice group) {}
    @Schema(name = "ForgeProjectImportRequest")
    public record ImportRequest(@NotNull UUID operationId,
                                @NotBlank @Pattern(regexp = "[a-f0-9]{64}") String context,
                                @NotNull @Size(min = 1, max = 25) List<@NotNull @Valid ImportItem> items) {
        public ImportRequest { items = items == null ? null : List.copyOf(items); }
    }
    public enum ImportStatus { CREATED, ALREADY_IMPORTED, FAILED }
    @Schema(name = "ForgeProjectImportResult")
    public record ImportResult(UUID itemId, String externalId, ImportStatus status, UUID projectId,
                               UUID repositoryId, UUID groupId,
                               @Schema(types = {"string", "null"}) String projectKey,
                               @Schema(types = {"string", "null"}) String groupKey,
                               String errorCode, boolean retryable) {}
    @Schema(name = "ForgeProjectImportResponse")
    public record ImportResponse(UUID operationId, List<ImportResult> items) {
        public ImportResponse { items = List.copyOf(items); }
    }
}
