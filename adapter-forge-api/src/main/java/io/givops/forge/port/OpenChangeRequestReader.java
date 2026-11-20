package io.givops.forge.port;

import io.givops.forge.model.ForgeModels.ChangeRequest;
import io.givops.forge.model.ForgeModels.Source;
import java.util.List;

/** Optional bounded read of every open, non-draft change request in a repository. */
public interface OpenChangeRequestReader {
    /**
     * Reads all target branches using the supplied access and request gate. This operation does not
     * observe pipelines, commit associations, discussions or approvals. Pipeline coverage remains
     * unknown; an empty result is a successful observation, while incomplete reads must fail.
     */
    List<ChangeRequest> readOpenChangeRequests(Source source, ForgeRequestGate gate, ForgeAdapter.Access access);
}
